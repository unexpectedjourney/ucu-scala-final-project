package ua.edu.ucu


import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.TextMessage
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Directives.{get, handleWebSocketMessages, pathPrefix, _}
import akka.stream.FlowShape
import akka.stream.scaladsl.GraphDSL.Implicits.{fanOut2flow, port2flow}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Sink, Source, Zip}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import spray.json._
import ua.edu.ucu.dto.Root
import ua.edu.ucu.dto.YahooStockJsonProtocol._
import ua.edu.ucu.integrations.StockPricesStreamSource
import ua.edu.ucu.integrations.YahooFinance.YahooFinance.lookForTickers
import ua.edu.ucu.utils.Configuration
import ua.edu.ucu.utils.TwitterFilter.lookForTweets

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.Random



object Application extends App {
  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val mapper: ObjectMapper = new ObjectMapper().registerModule(DefaultScalaModule)
  val envs = new Configuration()

  def checkIfTrendChanged(listOfPrices: Seq[Double]): Boolean = {
    if (listOfPrices(0) > listOfPrices(1) && listOfPrices(1) > listOfPrices(2) && listOfPrices(2) < listOfPrices(4)) true
    else if (listOfPrices(0) < listOfPrices(1) && listOfPrices(1) < listOfPrices(2) && listOfPrices(2) > listOfPrices(4)) true
    else if (Random.between(0, 3) == 2) true
    else false
  }


  val dictionary = Map("GOOGL" -> "GOOGLE",
                       "TSLA" -> "TESLA",
                       "MSFT" -> "MICROSOFT")


  def findTweets(symbol: String): String = {
    lookForTweets(dictionary(symbol))
  }

  def findNews(symbol: String): String = {
    lookForTickers(symbol)
  }

  val graphSource = StockPricesStreamSource(Seq("GOOGL", "TSLA", "MSFT")).via(GraphDSL.create() { implicit builder =>

    val in = builder.add(Broadcast[String](1))
    val bcast1 = builder.add(Broadcast[Tuple2[Seq[Double], String]](3))
    val bcast2 = builder.add(Broadcast[Tuple2[String, Boolean]](2))
    val bcast3 = builder.add(Broadcast[String](2))
    val zip1 = builder.add(Zip[String, Boolean])
    val zip2 = builder.add(Zip[String, String])
    val zip3 = builder.add(Zip[String, Double])
    val zip4 = builder.add(Zip[(String, Double), (String, String)])
    val out = builder.add(Merge[Map[String, Any]](1))


    val parseStockPrice = Flow[String].map(_.parseJson.convertTo[Root].apply())

    val latestPriceFlow =  Flow[(Seq[Double], String)].map(x => x._1 match {
      case x._1 if(x._1.length > 0) => x._1.last
      case _ => 0.00
    })
    val trendChangedFlow = Flow[(Seq[Double], String)].map(x => x._1 match {
      case x._1 if(x._1.length == 4) => checkIfTrendChanged(x._1)
      case _ => false
    })
    val getCompanyName = Flow[(Seq[Double], String)].map(x => x._2 match {
      case x._2 if (x._2 == null) => "unknown"
      case _ => x._2
    })

    val getTweetsIfNeededFlow = Flow[Tuple2[String, Boolean]].map(x => x._2 match {
      case true => s"Trend Changed, possible reasons: " + findTweets(x._1)
      case false => ""
    })

    val getNewsIfNeededFlow = Flow[Tuple2[String, Boolean]].map(x => x._2 match {
      case true => {
        findNews(x._1)
      }
      case false => ""
    })


    in ~> parseStockPrice ~> bcast1 ~> latestPriceFlow ~> zip3.in1
                             bcast1 ~> getCompanyName ~> bcast3 ~> zip1.in0
                                                          bcast3 ~> zip3.in0
                             bcast1 ~> trendChangedFlow ~> zip1.in1

    zip1.out ~> bcast2 ~> getTweetsIfNeededFlow ~> zip2.in0
               bcast2 ~> getNewsIfNeededFlow ~> zip2.in1

    zip3.out ~> zip4.in0
    zip2.out ~> zip4.in1
    zip4.out.map(s => Map("company" -> s._1._1, "price" -> s._1._2, "comment" -> (s._2._1, s._2._2))) ~> out

    FlowShape(in.in, out.out)
  })


  val serverSource: Source[Http.IncomingConnection, Future[Http.ServerBinding]] =
    Http().newServerAt("0.0.0.0", 8080).connectionSource()


  serverSource.runForeach { connection => // foreach materializes the source
    println("Accepted new connection from " + connection.remoteAddress)
    connection.handleWith(
      Directives.concat {
        get {
          pathPrefix("stream" / akka.http.scaladsl.server.PathMatchers.Segment) { symbol: String =>
            handleWebSocketMessages(Flow.fromSinkAndSource(Sink.ignore, graphSource.filter(x => x("company") == symbol).map(mapper.writeValueAsString(_)).map(TextMessage(_))))
          }
        }
      }
    )
  }

}
