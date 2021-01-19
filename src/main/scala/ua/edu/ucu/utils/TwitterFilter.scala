package ua.edu.ucu.utils

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.alpakka.mongodb.scaladsl.MongoSource
import akka.stream.scaladsl.{Sink, Source}
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.bson.{BsonDocument, ObjectId}
import org.mongodb.scala.model.Filters
import spray.json.DefaultJsonProtocol.{StringJsonFormat, immSeqFormat, mapFormat}
import spray.json.enrichAny

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

case class Tweet(
  _id: ObjectId,
  hashtag: String,
  body: String,
  timestamp: Long,
)


object TwitterFilter {
  private implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = Materializer(system)

  def lookForTweets(company: String): String = {

    val tweetsCollection = new Connection().getCollection("tweets", fromRegistries(fromProviders(classOf[Tweet]), DEFAULT_CODEC_REGISTRY))
    val source: Source[Tweet, _] =
      MongoSource(tweetsCollection.find(Filters.and(
          Filters.eq("hashtag", company),
          Filters.gte("timestamp", System.currentTimeMillis() - 900000)
        ), classOf[Tweet])
        .limit(5)
        .sort(BsonDocument("timestamp" -> -1)))

    val rows: Future[Seq[Tweet]] = source.runWith(Sink.seq)

    val res = Await
      .result(rows, 10.seconds)
      .map(item => {
        Map(
          "text" -> item.body.toString,
          "timestamp" -> item.timestamp.toString,
        )
      })
      .toJson
      .toString()

    res
  }

  def main(args: Array[String]): Unit = {
    print(lookForTweets("Tesla"))
  }
}
