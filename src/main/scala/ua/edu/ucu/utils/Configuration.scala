package ua.edu.ucu.utils

import java.util

class Configuration {
  val env: util.Map[String, String] = System.getenv()
  val twitterAuthConsumerKey: String = env.getOrDefault("TWITTER_AUTH_CONSUMER_KEY", "")
  val twitterAuthConsumerSecret: String = env.getOrDefault("TWITTER_AUTH_CONSUMER_SECRET", "")
  val twitterAuthAccessToken: String = env.getOrDefault("TWITTER_AUTH_ACCESS_TOKEN", "")
  val twitterAuthAccessTokenSecret: String = env.getOrDefault("TWITTER_AUTH_ACCESS_TOKEN_SECRET", "")

  val mongoInitdbRootUsername: String = env.getOrDefault("MONGO_INITDB_ROOT_USERNAME", "root")
  val mongoInitdbRootPassword: String = env.getOrDefault("MONGO_INITDB_ROOT_PASSWORD", "example")
}
