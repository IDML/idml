package io.idml.jsoup

import org.jsoup.Jsoup
import org.jsoup.parser.Parser

object PtolemyJsoup {

  /**
    * Parse an xml document
    */
  def parseXml(xml: String): JsoupElement =
    new JsoupElement(Jsoup.parse(xml, "", Parser.xmlParser()))

  /**
    * Parse a html document
    */
  def parseHtml(html: String): JsoupElement =
    new JsoupElement(Jsoup.parse(html, "", Parser.htmlParser()))

}
