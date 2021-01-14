package ua.edu.ucu.integrations

import akka.NotUsed
import akka.stream.scaladsl.{Broadcast, Concat, Flow, Merge, MergeSequence, RunnableGraph, Sink, Source, ZipWith}
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.stream.{ClosedShape, SourceShape}
import akka.stream.scaladsl.GraphDSL.Implicits.{SourceArrow, fanOut2flow, port2flow}

import scala.concurrent.duration.DurationInt
import spray.json._
import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.GraphDSL
import akka.stream.scaladsl.Zip
import ua.edu.ucu.dto.YahooStockJsonProtocol._
import akka.http.scaladsl.Http
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import ua.edu.ucu.dto.Root

object StockPricesStreamSource {

  def main(args: Array[String]): Unit = {


    implicit val system = ActorSystem()
    implicit val dispatcher = system.dispatcher


    def runRequest(req: HttpRequest) = {
      Http()
        .singleRequest(req).flatMap { response =>
        Unmarshal(response.entity).to[String]
      }
    }


    def checkIfTrendChanged(listOfPrices: Seq[Double]): Boolean = {
      if (listOfPrices(0) > listOfPrices(1) && listOfPrices(1) > listOfPrices(2) && listOfPrices(2) < listOfPrices(4)) true
      else if (listOfPrices(0) < listOfPrices(1) && listOfPrices(1) < listOfPrices(2) && listOfPrices(2) > listOfPrices(4)) true
      else false
    }

    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)

    def runPipeline2(company: String) = {
      RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
        val in = Source.repeat(HttpRequest(uri = Uri(s"https://query1.finance.yahoo.com/v7/finance/chart/$company?interval=1m&range=1m")))
          .throttle(1, 60.seconds)
          .mapAsync(1)(runRequest)
        val out = Sink.foreach(println)


        val bcast2 = builder.add(Broadcast[Seq[Double]](2))
        val zip = builder.add(Zip[Double, Boolean])


        val parseStockJson = Flow[String].map(_.parseJson.convertTo[Root].apply())
        val latestPriceFlow = Flow[Seq[Double]].map(x => x(3))
        val trendChangedFlow = Flow[Seq[Double]].map(checkIfTrendChanged(_))


        in ~> parseStockJson ~> bcast2 ~> latestPriceFlow ~> zip.in0
        bcast2 ~> trendChangedFlow ~> zip.in1

        zip.out.map(s => mapper.writeValueAsString(Map("company" -> company, "price" -> s._1, "trendChanged" -> s._2))) ~> out

        ClosedShape

      }).run()
    }

    val companies = Array("GOOGL", "TSLA", "SPCE")

    companies
      .foreach(runPipeline2)

  }



}
