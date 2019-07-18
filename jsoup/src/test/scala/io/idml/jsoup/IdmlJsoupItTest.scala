package io.idml.jsoup

import io.idml.datanodes._
import io.idml.IdmlValue
import io.idml.jackson.IdmlJackson
import org.scalatest.FunSuite

/** Integration tests for the IdmlJsoup library */
class IdmlJsoupItTest extends FunSuite {

  val root = IString("#root")

  def parse(xml: String): IdmlValue = {
    IdmlJackson.default.parse(IdmlJackson.default.compact(IdmlJsoup.parseXml(xml)))
  }

  test("An 'a' tag") {
    assert(parse("<a></a>") === IArray(root, IArray(IString("a"))))
  }

  test("A 'b' tag nested inside an 'a' tag") {
    assert(
      parse("<a><b></b></a>") === IArray(
        root,
        IArray(
          IString("a"),
          IArray(IString("b"))
        )
      ))
  }

  test("Multiple nested 'a' and 'b' tags") {
    assert(
      parse("<a><b></b><b></b></a><a><b></b><b></b></a>") === IArray(
        root,
        IArray(
          IString("a"),
          IArray(IString("b")),
          IArray(IString("b"))
        ),
        IArray(
          IString("a"),
          IArray(IString("b")),
          IArray(IString("b"))
        )
      ))
  }

  test("A tag with attributes") {
    assert(
      parse("<a w=\"x\" y=\"z\"></a>") === IArray(
        root,
        IArray(IString("a"), IObject("w" -> IString("x"), "y" -> IString("z")))
      ))
  }

  test("Multiple nested 'a' and 'b' tags with text") {
    assert(
      parse("<a><b></b>1<b>2</b></a>3<a><b></b>4<b></b></a>5") === IArray(
        root,
        IArray(
          IString("a"),
          IArray(IString("b")),
          IString("1"),
          IArray(IString("b"), IString("2"))
        ),
        IString("3"),
        IArray(
          IString("a"),
          IArray(IString("b")),
          IString("4"),
          IArray(IString("b"))
        ),
        IString("5")
      ))
  }

}
