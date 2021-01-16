package ua.edu.ucu.stages

import akka.NotUsed
import akka.stream.alpakka.mongodb.scaladsl.MongoSink
import akka.stream.scaladsl.{Flow, Sink}
import com.mongodb.reactivestreams.client.{MongoClients, MongoCollection}
import org.bson.Document
import ua.edu.ucu.dto.Tweet
import ua.edu.ucu.utils.Configuration

object MongoDBSink {
  val dbName = "final_project"
  val collectionName = "tweets"

  val config = new Configuration()
  val login: String = config.mongoInitdbRootUsername
  val password: String = config.mongoInitdbRootPassword
  val collection: MongoCollection[Document] = MongoClients
    .create(s"mongodb://$login:$password@mongodb:27017")
    .getDatabase(dbName)
    .getCollection(collectionName)

  def apply(): Sink[Tweet, NotUsed] = {
    Flow[Tweet]
      .map(tw => new Document()
        .append("timestamp", tw.timestamp)
        .append("body", tw.body)
      )
      .to(MongoSink.insertOne(collection))
  }
}
