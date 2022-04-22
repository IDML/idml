package io.idml.datanodes

import io.idml.{CastFailed, InvalidCaller, InvalidParameters}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

/** Test the behaviour of the PString class */
class IStringTest extends AnyFunSuite with Matchers {

  // Equality
  test("string == string")(new IString("a string") must equal(new IString("a string")))
  test("string != string")(
    new IString("a string".toUpperCase) must not equal new IString("a string"))

  // bool
  test("\"true\".bool() == PTrue")(new IString("true").bool() must equal(ITrue))
  test("\"yes\".bool() == PTrue")(new IString("yes").bool() must equal(ITrue))
  test("\"1\".bool() == PTrue")(new IString("1").bool() must equal(ITrue))
  test("\"false\".bool() == PFalse")(new IString("false").bool() must equal(IFalse))
  test("\"no\".bool() == PFalse")(new IString("no").bool() must equal(IFalse))
  test("\"0\".bool() == PFalse")(new IString("0").bool() must equal(IFalse))
  test("\"True\".bool() == PTrue")(new IString("True").bool() must equal(ITrue))
  test("\"YES\".bool() == PTrue")(new IString("YES").bool() must equal(ITrue))
  test("\"15\".bool() == CastFailed")(new IString("15").bool() must equal(CastFailed))
  test("\"-15\".bool() == CastFailed")(new IString("-15").bool() must equal(CastFailed))

  // format
  test("formatstring")(
    new IString("%s - %s")
      .format(new IString("hello"), new IString("world")) must equal(new IString("hello - world"))
  )
  test("bad format strings")(
    new IInt(42).format(new IString("argument one")) must equal(InvalidCaller))

  // int
  test("int")(new IString("42").int() must equal(new IInt(42)))
  test("negative int")(new IString("-7").int() must equal(new IInt(-7)))
  test("long-sized int")(
    new IString("598531285301272576").int() must equal(new IInt(598531285301272576L)))

  // plus
  test("plus")(new IString("hello ") + new IString("world") must equal(new IString("hello world")))
  test("plus int")(new IString("v") + new IInt(42) must equal(new IString("v42")))
  test("plus double")(new IString("D") + new IDouble(3.142) must equal(new IString("D3.142")))

  // capitalize
  test("capitalize")(new IString("hello world").capitalize() must equal(new IString("Hello World")))

  // normalize
  test("normalize - compose")(
    new IString("he\u0300llo world").normalize(IString("NFC")) must equal(
      IString("h\u00e8llo world")))
  test("normalize - decompose")(
    new IString("h\u00e8llo world").normalize(IString("NFD")) must equal(
      IString("he\u0300llo world")))
  test("normalize - bad form")(new IString("foo").normalize(IString("nope")) must equal(CastFailed))
}
