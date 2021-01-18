package ua.edu.ucu.integrations.YahooFinance

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, Uri}
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.Materializer

import scala.collection.immutable.Seq

object Crawler {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = Materializer(system)

  val qs = Query(
    "bkt" -> "openweb-finance-us-test1,fsd-qsp-ad-4",
    "crumb" -> "GexVm8xOe6/",
    "device" -> "desktop",
    "ecma" -> "modern",
    "feature" -> "adsMigration,canvassOffnet,ccOnMute,disableCommentsMessage,debouncesearch100,deferDarla,ecmaModern,emptyServiceWorker,enableCCPAFooter,enableCMP,enableConsentData,enableFeatureTours,enableFinancialsTemplate,enableFreeFinRichSearch,enableGuceJs,enableGuceJsOverlay,enableNavFeatureCue,enableNewResearchInsights,enablePfSummaryForEveryone,enablePremiumSingleCTA,enablePremiumScreeners,enablePrivacyUpdate,enableStreamDebounce,enableTheming,enableUpgradeLeafPage,enableVideoURL,enableXray,enableXrayInModal,enableXrayTickerEntities,enableYahooSans,enableYodleeErrorMsgCriOS,ncpListStream,ncpPortfolioStream,ncpQspStream,ncpStream,ncpStreamIntl,ncpTopicStream,newContentAttribution,newLogo,oathPlayer,optimizeSearch,relatedVideoFeature,threeAmigos,waferHeader,useNextGenHistory,videoNativePlaylist,sunsetMotif2,enableUserPrefAPI,livecoverage,darlaFirstRenderingVisible,enableTradeit,enableFeatureBar,enableSearchEnhancement,enableUserSentiment,enableBankrateWidget,enableYodlee,canvassReplies,enablePremiumFinancials,enableInstapage,enableNewResearchFilterMW,showExpiredIdeas,showMorningStar,enableSingleRail,enhanceAddToWL,article2_csn,sponsoredAds,enableStageAds,enableTradeItLinkBrokerSecondaryPromo,premiumPromoHeader,enableQspPremiumPromoSmall,clientDelayNone,threeAmigosMabEnabled,threeAmigosAdsEnabledAndStreamIndex0,enableRelatedTickers,enableNotification,enableQuoteSearchAd4",
    "intl" -> "us",
    "lang" -> "en-US",
    "partner" -> "none",
    "prid" -> "aiim52lfvp6dd",
    "region" -> "US",
    "site" -> "finance",
    "tz" -> "Europe/Kiev",
    "ver" -> "0.102.4250",
  )

  val headers = Seq(
    RawHeader("Host", "finance.yahoo.com"),
    RawHeader("cookie", "B=52dq3b1fg3u88&b=3&s=fm; A1=d=AQABBAj5AV8CEH0ecaqzzsvhpw2EtFhDN1EFEgEBAgFl_F_gYMzQb2UB_SMAAAcICPkBX1hDN1E&S=AQAAAlsXXCSPijiRNaBpGL0XoaE; A3=d=AQABBAj5AV8CEH0ecaqzzsvhpw2EtFhDN1EFEgEBAgFl_F_gYMzQb2UB_SMAAAcICPkBX1hDN1E&S=AQAAAlsXXCSPijiRNaBpGL0XoaE; A1S=d=AQABBAj5AV8CEH0ecaqzzsvhpw2EtFhDN1EFEgEBAgFl_F_gYMzQb2UB_SMAAAcICPkBX1hDN1E&S=AQAAAlsXXCSPijiRNaBpGL0XoaE&j=WORLD; GUC=AQEBAgFf_GVg4EIbOAPo; PRF=t%3D%255EIXIC%252B%255EDJI%252BLOCM; cmp=t=1610958003&j=0"),
  )

  val payload = "{\"requests\":{\"g0\":{\"resource\":\"StreamService\",\"operation\":\"read\",\"params\":{\"ui\":{\"link_out_allowed\":true,\"pubtime_maxage\":0,\"summary\":true},\"useNCP\":true,\"batches\":{\"pagination\":true,\"size\":200,\"timeout\":1500,\"total\":200},\"content_site\":\"finance\"}}}}"

  val request = HttpRequest(
    method = HttpMethods.POST,
    uri = Uri("/_finance_doubledown/api/resource").withQuery(qs),
    entity = HttpEntity(
      ContentTypes.`application/json`,
      payload
    )
  ).withHeaders(headers)

  val requestFlow = Http(system).newHostConnectionPoolHttps[Int](host = "finance.yahoo.com")

}
