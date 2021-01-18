package ua.edu.ucu.utils

import java.util

class Configuration {
  val env: util.Map[String, String] = System.getenv()
  val twitterAuthConsumerKey: String = env.getOrDefault("TWITTER_AUTH_CONSUMER_KEY", "HIS2AzJ49IVWfejfZDG0TZTIf")
  val twitterAuthConsumerSecret: String = env.getOrDefault("TWITTER_AUTH_CONSUMER_SECRET", "j9R1esaAWxMIbGY9fCk0KEFyWqSmJyuUfQ2LD2djpWz6eI71HR")
  val twitterAuthAccessToken: String = env.getOrDefault("TWITTER_AUTH_ACCESS_TOKEN", "709079447-VcTlgLoVodn5a3JyAjYVJA1spfxPzDnp2imdKMbW")
  val twitterAuthAccessTokenSecret: String = env.getOrDefault("TWITTER_AUTH_ACCESS_TOKEN_SECRET", "VAOPnJ1gJWWeYYZK2TquHHC2YIhaodVwkQLz2KSyv5qnu")

  val mongoInitdbRootUsername: String = env.getOrDefault("MONGO_INITDB_ROOT_USERNAME", "root")
  val mongoInitdbRootPassword: String = env.getOrDefault("MONGO_INITDB_ROOT_PASSWORD", "example")
}
