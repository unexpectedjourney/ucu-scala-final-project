package ua.edu.ucu.utils

import com.mongodb.reactivestreams.client.{MongoClients, MongoCollection}
import org.bson.Document

class Connection {
  def getMongoConnection(dbName: String, collectionName: String) = {
    val config = new Configuration()
    val login: String = config.mongoInitdbRootUsername
    val password: String = config.mongoInitdbRootPassword
    val collection: MongoCollection[Document] = MongoClients
      .create(s"mongodb://$login:$password@mongodb:27017")
      .getDatabase(dbName)
      .getCollection(collectionName)

    collection
  }
}
