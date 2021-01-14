package ua.edu.ucu

import integrations.TwitterStreamSource
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.TextMessage
import akka.http.scaladsl.server.Directives.{concat, get, getFromResource, handleWebSocketMessages, path}
import akka.stream.FlowShape
import akka.stream.scaladsl.GraphDSL.Implicits.fanOut2flow
import akka.stream.scaladsl.{Broadcast, BroadcastHub, Flow, GraphDSL, Keep, Merge, Sink, Source}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import twitter4j.conf.ConfigurationBuilder
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
      val IN = graphBuilder.add(Broadcast[String](1))
      val OUT = graphBuilder.add(Merge[String](1))

      IN ~> OUT

      FlowShape(IN.in, OUT.out)
    })
    .toMat(BroadcastHub.sink)(Keep.right)
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
              getFromResource("./ui/index.html")
            },
            path("main.js") {
              getFromResource("./ui/main.js")
            },
            path("stream") {
              handleWebSocketMessages(Flow.fromSinkAndSource(Sink.ignore, graphSource.map(mapper.writeValueAsString(_)).map(TextMessage(_))))
            }
          )
        }
      )
    }
}
