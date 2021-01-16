package ua.edu.ucu.stages

import akka.NotUsed
import akka.stream.alpakka.mongodb.scaladsl.MongoSink
import akka.stream.scaladsl.{Flow, Sink}
import com.mongodb.reactivestreams.client.MongoCollection
import org.bson.Document
import ua.edu.ucu.dto.Tweet
import ua.edu.ucu.utils.Connection

object MongoDBSink {
  val dbName = "final_project"
  val collectionName = "tweets"
  val collection: MongoCollection[Document] = new Connection().getMongoConnection(dbName, collectionName)

  def apply(): Sink[Tweet, NotUsed] = {
    Flow[Tweet]
      .map(tw => new Document()
        .append("timestamp", tw.timestamp)
        .append("body", tw.body)
      )
      .to(MongoSink.insertOne(collection))
  }
}
