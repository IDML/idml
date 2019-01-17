package io.idml.jsoup

import io.idml.MissingField
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import org.jsoup.select.Elements
import org.scalatest.{FunSuite, MustMatchers}

import scala.language.implicitConversions

/** Tests the XmlDocument class */
class JsoupElementTest extends FunSuite with MustMatchers {

  /** Convert a string to an element */
  implicit def stringToElement(str: String): Element =
    Jsoup.parse(str, "", Parser.xmlParser())

  /** Convert a string to a collection of elements */
  implicit def stringToElements(str: String): Elements =
    new Elements(Jsoup.parse(str, "", Parser.xmlParser()).child(0))

  test("Constructor rejects null elements") {
    intercept[IllegalArgumentException](new JsoupElement(null))
  }

  test("get(attribs) returns JsoupAttributes object") {
    new JsoupElement("<a>abc</a>").get("attribs").getClass must equal(classOf[JsoupAttributes])
  }

  test("get(missing_tag) returns missing tag") {
    new JsoupElement("<a>abc</a><b>def</b>").get("c") must equal(MissingField)
  }

  test("get(nested_tag) returns nested tag") {
    new JsoupElement("<a>abc</a><b>def</b>").get("b") must equal(new JsoupSelection("<b>def</b>"))
  }

  test("get(deeply_nested_tag) returns missing tag") {
    new JsoupElement("<a>abc<b>def</b></a>").get("b") must equal(MissingField)
  }

  test("get(many_nested_tags) returns nested tags") {
    new JsoupElement("<a>abc</a><b>def</b><a>hij</a><b>lmn</b>")
      .get("b")
      .string() must equal(new JsoupElement("<b>def</b> <b>lmn</b>").string())
  }

}
