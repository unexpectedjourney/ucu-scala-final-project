package ua.edu.ucu.dto

import com.typesafe.config.ConfigFactory

import scala.collection.Set

final case class Author(handle: String)

final case class Hashtag(name: String)

case class Tweet(author: Author, timestamp: Long, body: String) {
  def hashtags: Set[Hashtag] =
    body.split(" ").collect { case t if t.startsWith("#") => Hashtag(t)}.toSet
}

final object EmptyTweet extends Tweet(Author(""), 0L, "")

object CredentialsUtils {
  val configData = ConfigFactory.load()
  val appKey: String = configData.getString("appKey")
  val appSecret: String = configData.getString("appSecret")
  val accessToken: String = configData.getString("accessToken")
  val accessTokenSecret: String = configData.getString("accessTokenSecret")
}