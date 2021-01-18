package ua.edu.ucu.integrations.YahooFinance

import spray.json.DefaultJsonProtocol


case class Ticker (
  symbol: String,
)

case class Tickers (
  stockTickers: Option[List[Ticker]],
)

case class Article(
  title: String,
  id: String,
  summary: Option[String],
  url: String,
  finance: Tickers,
  pubtime: Long,
)

case class Data(
  stream_items: List[Article],
)

case class G0(
  data: Data,
)

case class Root(
  g0: G0,
)

object YahooFinanceJsonProtocol extends DefaultJsonProtocol {
  implicit val ticker = jsonFormat1(Ticker)
  implicit val tickers = jsonFormat1(Tickers)
  implicit val article = jsonFormat6(Article)
  implicit val data = jsonFormat1(Data)
  implicit val g0 = jsonFormat1(G0)
  implicit val root = jsonFormat1(Root)
}

