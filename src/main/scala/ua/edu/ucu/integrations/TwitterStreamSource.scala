package ua.edu.ucu.integrations

import akka.NotUsed
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import twitter4j._
import twitter4j.conf.Configuration
import ua.edu.ucu.dto.{Author, Hashtag, Tweet}

import scala.concurrent.{ExecutionContext, Future}

object TwitterStreamSource {
  def apply(companies: Seq[String], config: Configuration): Source[Tweet, Future[NotUsed]] = {
    Source.fromMaterializer((mat, a) => {
      implicit val context: ExecutionContext.parasitic.type = ExecutionContext.parasitic
      val (queue, source) = Source.queue[Tweet](256, OverflowStrategy.dropHead).preMaterialize()(mat)
      val twitterStream: TwitterStream = new TwitterStreamFactory(config).getInstance()

      queue.watchCompletion()
        .onComplete(_ => {
          twitterStream.cleanUp
          twitterStream.shutdown
        })

      val companyFilters = new FilterQuery().track(companies:_*)
      val statusListener = new StatusListener() {

        override def onStatus(status: Status): Unit = {
          companies.filter(hashtag => status.getText contains hashtag).foreach(
            hashtag => {
              queue.offer(
              Tweet(
                Author(status.getUser.getScreenName),
                status.getCreatedAt.getTime,
                status.getText,
                Hashtag(hashtag)))}
          )
        }

        override def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = {}

        override def onTrackLimitationNotice(numberOfLimitedStatuses: Int): Unit = {}

        override def onException(ex: Exception): Unit = ex.printStackTrace()

        override def onScrubGeo(userId: Long, upToStatusId: Long): Unit = {}

        override def onStallWarning(warning: StallWarning): Unit = {}
      }
      twitterStream.addListener(statusListener)

      twitterStream.filter(companyFilters)
      source
    }).async
  }
}


