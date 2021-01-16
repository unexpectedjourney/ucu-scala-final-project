package ua.edu.ucu.integrations

import akka.NotUsed
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Sink, Source, Zip}
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.stream.FlowShape
import akka.stream.scaladsl.GraphDSL.Implicits.{fanOut2flow, port2flow}
import scala.concurrent.duration.DurationInt
import spray.json._
import akka.actor.ActorSystem
import ua.edu.ucu.dto.YahooStockJsonProtocol._
import akka.http.scaladsl.server.Directives.{get, path}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.TextMessage
import akka.http.scaladsl.server.Directives.handleWebSocketMessages
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import ua.edu.ucu.dto.Root
import scala.concurrent.Future
import scala.util.Random

object StockPricesStreamSource {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val dispatcher = system.dispatcher


    def checkIfTrendChanged(listOfPrices: Seq[Double]): Boolean = {
      if (listOfPrices(0) > listOfPrices(1) && listOfPrices(1) > listOfPrices(2) && listOfPrices(2) < listOfPrices(4)) true
      else if (listOfPrices(0) < listOfPrices(1) && listOfPrices(1) < listOfPrices(2) && listOfPrices(2) > listOfPrices(4)) true
      else if (Random.between(0, 3) == 2) true
      else false
    }

    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)

    def runRequest(req: HttpRequest) = {
      Http()
        .singleRequest(req).flatMap { response =>
        Unmarshal(response.entity).to[String]
      }
    }

    def makeSource(company: String): Source[String, NotUsed] = {
      Source.repeat(HttpRequest(uri = Uri(s"https://query1.finance.yahoo.com/v7/finance/chart/$company?interval=1m&range=4m")))
        .throttle(1, 60.seconds)
        .mapAsync(1)(runRequest)
    }

    def combineSources(companies: Seq[String], n: Int, state: Source[String, NotUsed]): Source[String, NotUsed] = {
      if (n < 0) state
      else combineSources(companies, n-1, Source.combine(makeSource(companies(n)), state)(Merge(_)))
    }

    val companies = Seq("GOOGL", "TSLA", "MSFT")

    val stockSource =  combineSources(companies, companies.length-1, Source.empty)

    val graphSource = stockSource.via(GraphDSL.create() { implicit builder =>

      val in = builder.add(Broadcast[String](1))
      val bcast = builder.add(Broadcast[Tuple2[Seq[Double], String]](3))
      val zip = builder.add(Zip[Double, Boolean])
      val zip2 = builder.add(Zip[(Double, Boolean), String])
      val out = builder.add(Merge[String](1))


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

      in ~> parseStockPrice ~> bcast ~> latestPriceFlow ~> zip.in0
                               bcast ~> trendChangedFlow ~> zip.in1
                               bcast ~> getCompanyName ~> zip2.in1
      zip.out ~> zip2.in0
      zip2.out.map(s => mapper.writeValueAsString(Map("company" -> s._2, "price" -> s._1._1, "trendChanged" -> s._1._2))) ~> out

      FlowShape(in.in, out.out)
    })

    val serverSource: Source[Http.IncomingConnection, Future[Http.ServerBinding]] =
      Http().newServerAt("localhost", 8080).connectionSource()

    serverSource.runForeach { connection => // foreach materializes the source
      println("Accepted new connection from " + connection.remoteAddress)
      connection.handleWith(
        get {
          path("stream") {
            handleWebSocketMessages(Flow.fromSinkAndSource(Sink.ignore, graphSource.map(mapper.writeValueAsString(_)).map(TextMessage(_))))
          }
        }
      )
    }



  }
}
