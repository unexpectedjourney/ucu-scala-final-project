package ua.edu.ucu.dto

import spray.json.DefaultJsonProtocol


case class Root(chart: Chart) {
  def apply() = Tuple2(chart(), chart.getSymbol())
}

case class Chart(result: List[Result]) {
  def apply() = result(0)()
  def getSymbol() = result(0).getSymbol()
}

case class Result(indicators: Indicators,
                  meta: Meta) {
  def apply() = indicators()
  def getSymbol() = meta()
}


case class Indicators(quote: List[Quote]) {
  def apply() = quote(0)()
}

case class Quote(close: Seq[Double]) {
  def apply() = close
}

case class Meta(symbol: String) {
  def apply() = symbol
}

object YahooStockJsonProtocol extends DefaultJsonProtocol {
  implicit val quoteFormat = jsonFormat1(Quote)
  implicit val indicatorsFormat = jsonFormat1(Indicators)
  implicit val metaFormat = jsonFormat1(Meta)
  implicit val resultFormat = jsonFormat2(Result)
  implicit val chartFormat = jsonFormat1(Chart)
  implicit val rootFormat = jsonFormat1(Root)

}
