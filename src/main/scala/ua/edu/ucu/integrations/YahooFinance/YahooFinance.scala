package ua.edu.ucu.integrations.YahooFinance

import spray.json._
import ua.edu.ucu.utils.Connection
import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import akka.stream.alpakka.mongodb.scaladsl.MongoSink
import akka.stream.alpakka.mongodb.DocumentUpdate
import akka.stream.scaladsl.{Flow, Source}
import com.mongodb.client.model.UpdateOptions
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.{Filters, Updates}
import ua.edu.ucu.integrations.YahooFinance.YahooFinanceJsonProtocol._

import scala.concurrent.duration._
import scala.util.{Success, Try}


object YahooFinance {
  private implicit val system = ActorSystem()
  private implicit val materializer = Materializer(system)
  import system.dispatcher

  private val articlesCollection = new Connection().getCollection("articles", fromRegistries(fromProviders(classOf[Article]), DEFAULT_CODEC_REGISTRY))

  val source = Source.tick(0.seconds, 2.minutes, (Crawler.request, 0))
  val requestFlow = Crawler.requestFlow

  val processFlow = Flow[(Try[HttpResponse], _)]
    .mapAsync(1) {
      case (Success(r), _ ) =>
        Unmarshal(r.entity)
          .to[String]
          .map(_.parseJson)
          .map(_.convertTo[Root].g0.data.stream_items)
    }

  val updateFlow = Flow[List[Article]]
    .flatMapConcat(articles =>
      Source(articles.map(article => {
        DocumentUpdate(
          filter = Filters.eq("id", article.id),
          update = Updates.combine(
            Updates.setOnInsert("title", article.title),
            Updates.setOnInsert("url", article.url),
            Updates.setOnInsert("pubtime", article.pubtime),
            Updates.setOnInsert("summary", article.summary.orNull),
            Updates.setOnInsert("tickers", article.finance.stockTickers.getOrElse(List.empty).map(_.symbol)),
          ),
        )
      }))
    )

  def main(args: Array[String]): Unit = {
    source
      .via(requestFlow)
      .via(processFlow)
      .via(updateFlow)
      .runWith(MongoSink.updateMany(articlesCollection, options = new UpdateOptions().upsert(true)))
  }
}
