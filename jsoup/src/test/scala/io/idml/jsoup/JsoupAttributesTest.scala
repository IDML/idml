package io.idml.jsoup

import io.idml.datanodes.PString
import io.idml.MissingField
import org.jsoup.Jsoup
import org.jsoup.nodes.Attributes
import org.jsoup.parser.Parser
import org.scalatest.{FunSuite, MustMatchers}

import scala.language.implicitConversions

/** Verifies the behaviour of the JsoupAttributes class */
class JsoupAttributesTest extends FunSuite with MustMatchers {

  /** Convert a string to a collection of elements */
  implicit def stringToChildren(str: String): Attributes =
    Jsoup.parse(str, "", Parser.xmlParser()).child(0).attributes()

  test("Constructor rejects null elements") {
    intercept[IllegalArgumentException](new JsoupAttributes(null))
  }

  test("fields() returns a map") {
    new JsoupAttributes("<a b=\"123\" c=\"456\">abc</a>").fields must equal(
      Map(
        "b" -> PString("123"),
        "c" -> PString("456")
      ))
  }

  test("get(missing_attrib) returns missing field") {
    new JsoupAttributes("<a b=\"123\">abc</a>").get("c") must equal(MissingField)
  }

  test("get(attrib) returns attrib") {
    new JsoupAttributes("<a b=\"123\" c=\"456\">abc</a>").get("c") must equal(PString("456"))
  }
}
