package io.idml.jsoup

import io.idml.datanodes._
import io.idml.{PtolemyJson, PtolemyValue}
import org.scalatest.FunSuite

/** Integration tests for the PtolemyJsoup library */
class PtolemyJsoupItTest extends FunSuite {

  val root = PString("#root")

  def parse(xml: String): PtolemyValue = {
    PtolemyJson.parse(PtolemyJson.compact(PtolemyJsoup.parseXml(xml)))
  }

  test("An 'a' tag") {
    assert(parse("<a></a>") === PArray(root, PArray(PString("a"))))
  }

  test("A 'b' tag nested inside an 'a' tag") {
    assert(
      parse("<a><b></b></a>") === PArray(
        root,
        PArray(
          PString("a"),
          PArray(PString("b"))
        )
      ))
  }

  test("Multiple nested 'a' and 'b' tags") {
    assert(
      parse("<a><b></b><b></b></a><a><b></b><b></b></a>") === PArray(
        root,
        PArray(
          PString("a"),
          PArray(PString("b")),
          PArray(PString("b"))
        ),
        PArray(
          PString("a"),
          PArray(PString("b")),
          PArray(PString("b"))
        )
      ))
  }

  test("A tag with attributes") {
    assert(
      parse("<a w=\"x\" y=\"z\"></a>") === PArray(
        root,
        PArray(PString("a"), PObject("w" -> PString("x"), "y" -> PString("z")))
      ))
  }

  test("Multiple nested 'a' and 'b' tags with text") {
    assert(
      parse("<a><b></b>1<b>2</b></a>3<a><b></b>4<b></b></a>5") === PArray(
        root,
        PArray(
          PString("a"),
          PArray(PString("b")),
          PString("1"),
          PArray(PString("b"), PString("2"))
        ),
        PString("3"),
        PArray(
          PString("a"),
          PArray(PString("b")),
          PString("4"),
          PArray(PString("b"))
        ),
        PString("5")
      ))
  }

}
