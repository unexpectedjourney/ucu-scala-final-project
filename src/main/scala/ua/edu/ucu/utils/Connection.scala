package ua.edu.ucu.utils

import com.mongodb.reactivestreams.client.{MongoClients, MongoCollection}
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry

class Connection {
  def getMongoConnection(dbName: String, collectionName: String) = {
    val config = new Configuration()
    val login: String = config.mongoInitdbRootUsername
    val password: String = config.mongoInitdbRootPassword
    val collection: MongoCollection[Document] = MongoClients
      .create(s"mongodb://$login:$password@mongo:27017")
      .getDatabase(dbName)
      .getCollection(collectionName)

    collection
  }

  private val client = MongoClients.create(scala.util.Properties.envOrElse("MONGO_CONNECTION_STRING", "mongodb://root:example@localhost:27017"))
  private val db = client.getDatabase(scala.util.Properties.envOrElse("MONGO_DATABASE_NAME", "final_project"))

  def getCollection(collection: String, codecRegistry: CodecRegistry) = {
    db
      .getCollection(collection)
      .withCodecRegistry(codecRegistry)
  }
}
