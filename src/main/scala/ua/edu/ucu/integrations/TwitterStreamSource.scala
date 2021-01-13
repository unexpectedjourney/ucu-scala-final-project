package ua.edu.ucu.integrations

import akka.NotUsed
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import twitter4j.conf.Configuration
import twitter4j._

import scala.concurrent.{ExecutionContext, Future}

object TwitterStreamSource {
//  def apply(searchQuery: String, config: Configuration): Source[Tweet, Future[NotUsed]] = {
  def apply(searchQuery: String, config: Configuration): Source[String, Future[NotUsed]] = {
    Source.fromMaterializer((mat, a) => {
      println(s"Here $mat")
      implicit val context: ExecutionContext.parasitic.type = ExecutionContext.parasitic
//      val (queue, source) = Source.queue[Tweet](256, OverflowStrategy.dropHead).preMaterialize()(mat)
      val (queue, source) = Source.queue[String](256, OverflowStrategy.dropHead).preMaterialize()(mat)
      val twitterStream = new TwitterStreamFactory(config).getInstance()

      queue.watchCompletion()
        .onComplete(_ =>{
          println("here")
          twitterStream.cleanUp
          twitterStream.shutdown
        })

      val statusListener = new StatusListener() {

        override def onStatus(status: Status): Unit = {
//          queue.offer(Tweet(Author(status.getUser.getScreenName), status.getCreatedAt.getTime, status.getText))
          println(status.getText)
          queue.offer(status.getText)
        }

        override def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = {}

        override def onTrackLimitationNotice(numberOfLimitedStatuses: Int): Unit = {}

        override def onException(ex: Exception): Unit = ex.printStackTrace()

        override def onScrubGeo(userId: Long, upToStatusId: Long): Unit = {}

        override def onStallWarning(warning: StallWarning): Unit = {}

      }
      twitterStream.addListener(statusListener)
      twitterStream.filter(searchQuery)

      source
    }).async
  }
}


