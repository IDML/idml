package io.idml.jackson

import io.idml.{IdmlNull, IdmlValue}
import io.idml.datanodes.{IArray, IInt, IObject, IString}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

import scala.collection.mutable

class ObjectParsingSpec extends AnyFunSuite with Matchers {
  import IdmlJackson.default._

  // Parsing
  test("parse empty")(parse("{}") must equal(IObject()))
  test("parse with primitives")(parse("""{"s": "abc", "i": 123, "f": 123.4, "n":null}""") must equal(withPrimitives))
  test("parse with nested objects")(parse("""{"o": {"s": "abc", "i": 123, "f": 123.4, "n": null}}""") must equal(withNestedObjects))
  test("parse with nested arrays")(parse("""{"a": ["abc", 123, 123.4, null]}""") must equal(withNestedArrays))

  // Generation
  test("generate empty")(parse("{}") must equal(pc(IObject())))
  test("generate with primitives")(parse("""{"s": "abc", "i": 123, "f": 123.4, "n":null}""") must equal(pc(withPrimitives)))
  test("generate with nested objects")(parse("""{"o": {"s": "abc", "i": 123, "f": 123.4, "n": null}}""") must equal(pc(withNestedObjects)))
  test("generate with nested arrays")(parse("""{"a": ["abc", 123, 123.4, null]}""") must equal(pc(withNestedArrays)))

  /*
  // serialize
  test("serialize should serialize")(
    PObject("test" -> PInt(2)).serialize() must equal(PString("""{"test":2}"""))
  )

  // parse json
  test("parse json")(PString("{\"key\":\"value\"}\"}").parseJson() must equal(PObject("key" -> PString("value"))))
  test("parse a json number")(PString("4").parseJson() must equal(PInt(4)))
  test("parse json string")(PString("\"hello\"").parseJson() must equal(PString("hello")))
   */
  // Test data

  def pc = compact _ andThen parse

  def withPrimitives =
    new IObject(
      mutable.Map(
        "s" -> IdmlValue("abc"),
        "i" -> IdmlValue(123),
        "f" -> IdmlValue(123.4),
        "n" -> IdmlNull
      ))

  def withNestedObjects =
    new IObject(
      mutable.Map(
        "o" -> new IObject(
          mutable.Map(
            "s" -> IdmlValue("abc"),
            "i" -> IdmlValue(123),
            "f" -> IdmlValue(123.4),
            "n" -> IdmlNull
          ))))

  def withNestedArrays =
    new IObject(
      mutable.Map(
        "a" -> new IArray(
          mutable.ListBuffer(
            IdmlValue("abc"),
            IdmlValue(123),
            IdmlValue(123.4),
            IdmlNull
          ))))
}
