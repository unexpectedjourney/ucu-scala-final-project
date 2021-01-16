package ua.edu.ucu

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, concat, get, path}
import akka.stream.FlowShape
import akka.stream.scaladsl.GraphDSL.Implicits.fanOut2flow
import akka.stream.scaladsl.{Broadcast, BroadcastHub, GraphDSL, Keep, Merge, Source}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import twitter4j.conf.ConfigurationBuilder
import ua.edu.ucu.dto.Tweet
import ua.edu.ucu.integrations.TwitterStreamSource
import ua.edu.ucu.stages.MongoDBSink
import ua.edu.ucu.utils.Configuration

import scala.concurrent.{ExecutionContextExecutor, Future}

object Application extends App {
  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val mapper: ObjectMapper = new ObjectMapper().registerModule(DefaultScalaModule)
  val envs = new Configuration()
  val config = new ConfigurationBuilder()
    .setDebugEnabled(true)
    .setOAuthConsumerKey(envs.twitterAuthConsumerKey)
    .setOAuthConsumerSecret(envs.twitterAuthConsumerSecret)
    .setOAuthAccessToken(envs.twitterAuthAccessToken)
    .setOAuthAccessTokenSecret(envs.twitterAuthAccessTokenSecret)
    .build()

  private val graphSource = TwitterStreamSource("Tesla", config)
    .via(GraphDSL.create() { implicit graphBuilder =>
      val IN = graphBuilder.add(Broadcast[Tweet](1))
      val TWEET = graphBuilder.add(Broadcast[Tweet](2))
      val OUT = graphBuilder.add(Merge[Tweet](1))

      IN ~> TWEET                  ~> OUT
            TWEET ~> MongoDBSink()

      FlowShape(IN.in, OUT.out)
    })
    .toMat(BroadcastHub.sink)(Keep.left)
    .run

  val serverSource: Source[Http.IncomingConnection, Future[Http.ServerBinding]] =
    Http().newServerAt("localhost", 8080).connectionSource()

  val bindingFuture =
    serverSource.runForeach { connection =>
      println("Accepted new connection from " + connection.remoteAddress)
      connection.handleWith(
        get {
          concat(
            path("") {
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
            }
          )
        }
      )
    }
}
