package ua.edu.ucu.integrations.YahooFinance


import spray.json._
import java.util.TimeZone
import java.text.SimpleDateFormat
import ua.edu.ucu.utils.Connection
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.alpakka.mongodb.scaladsl.{MongoSink, MongoSource}
import akka.stream.scaladsl.{Flow, Sink, Source}
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.codecs.Macros._
import ua.edu.ucu.integrations.YahooFinance.YahooFinanceJsonProtocol._

import java.util.Date
import scala.concurrent.Await
import scala.concurrent.duration._


object YahooFinance {
  private implicit val system = ActorSystem()
  implicit val materializer = Materializer(system)

  def lookForTickers(ticker: String): String = {
    case class StoredArticle(
      title: String,
      id: String,
      summary: Option[String],
      url: String,
      tickers: Option[List[String]],
      pubtime: Long,
    )

    val articlesCollection = new Connection().getCollection("articles", fromRegistries(fromProviders(classOf[StoredArticle]), DEFAULT_CODEC_REGISTRY))
    val source: Source[StoredArticle, _] =
      MongoSource(articlesCollection.find(classOf[StoredArticle])
        .limit(5)
        .sort(BsonDocument("pubtime" -> -1))
        .filter(BsonDocument("tickers" -> ticker)))

    val rows =
      source
        .runWith(Sink.seq[StoredArticle])

    val res = Await
      .result(rows, 10.seconds)
      .map(item => {
        Map(
          "title" -> item.title,
          "pubtime" ->  {
            val dt = new Date(item.pubtime)
            val sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"))
            sdf.format(dt)
          },
          "url" -> item.url,
        )
      })
      .toJson
      .toString()

    res
  }
}
