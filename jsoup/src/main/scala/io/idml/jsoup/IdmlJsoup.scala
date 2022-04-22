package io.idml.jsoup

import io.idml.IdmlValue
import org.jsoup.Jsoup
import org.jsoup.parser.Parser

object IdmlJsoup {

  /** Parse an xml document
    */
  def parseXml(xml: String): IdmlValue =
    JsoupConverter(Jsoup.parse(xml, "", Parser.xmlParser()))

  /** Parse a html document
    */
  def parseHtml(html: String): IdmlValue =
    JsoupConverter(Jsoup.parse(html, "", Parser.htmlParser()))

}
