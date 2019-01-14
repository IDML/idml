package io.idml.datanodes

import io.idml.{CastFailed, InvalidCaller, InvalidParameters, PtolemyJson}
import org.scalatest._

/** Test the behaviour of the PString class */
class PStringTest extends FunSuite with MustMatchers {
  import PtolemyJson._

  // Parsing
  test("parse string")(PtolemyJson.parse("\"a string\"") must equal(new PString("a string")))

  // Generation
  test("generate string")("\"a string\"" must equal(compact(new PString("a string"))))

  // Equality
  test("string == string")(new PString("a string") must equal(new PString("a string")))
  test("string != string")(new PString("a string".toUpperCase) must not equal new PString("a string"))

  // bool
  test("\"true\".bool() == PTrue")(new PString("true").bool() must equal(PTrue))
  test("\"yes\".bool() == PTrue")(new PString("yes").bool() must equal(PTrue))
  test("\"1\".bool() == PTrue")(new PString("1").bool() must equal(PTrue))
  test("\"false\".bool() == PFalse")(new PString("false").bool() must equal(PFalse))
  test("\"no\".bool() == PFalse")(new PString("no").bool() must equal(PFalse))
  test("\"0\".bool() == PFalse")(new PString("0").bool() must equal(PFalse))
  test("\"True\".bool() == PTrue")(new PString("True").bool() must equal(PTrue))
  test("\"YES\".bool() == PTrue")(new PString("YES").bool() must equal(PTrue))
  test("\"15\".bool() == CastFailed")(new PString("15").bool() must equal(CastFailed))
  test("\"-15\".bool() == CastFailed")(new PString("-15").bool() must equal(CastFailed))

  // format
  test("formatstring")(
    new PString("%s - %s")
      .format(new PString("hello"), new PString("world")) must equal(new PString("hello - world")))
  test("bad format strings")(new PInt(42).format(new PString("argument one")) must equal(InvalidCaller))

  // int
  test("int")(new PString("42").int() must equal(new PInt(42)))
  test("negative int")(new PString("-7").int() must equal(new PInt(-7)))
  test("long-sized int")(new PString("598531285301272576").int() must equal(new PInt(598531285301272576L)))

  // plus
  test("plus")(new PString("hello ") + new PString("world") must equal(new PString("hello world")))
  test("plus int")(new PString("v") + new PInt(42) must equal(new PString("v42")))
  test("plus double")(new PString("D") + new PDouble(3.142) must equal(new PString("D3.142")))

  //random
  test("random")(new PString("hello").random() must equal(PInt(1816935814987899352L)))

  // capitalize
  test("capitalize")(new PString("hello world").capitalize() must equal(new PString("Hello World")))
}
