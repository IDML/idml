package io.idml.jackson

import io.idml.{IdmlNull, IdmlValue}
import io.idml.datanodes.{PArray, PInt, PObject, PString}
import org.scalatest.{FunSuite, MustMatchers}

import scala.collection.mutable

class ObjectParsingSpec extends FunSuite with MustMatchers {
  import IdmlJackson.default._

  // Parsing
  test("parse empty")(parse("{}") must equal(PObject()))
  test("parse with primitives")(parse("""{"s": "abc", "i": 123, "f": 123.4, "n":null}""") must equal(withPrimitives))
  test("parse with nested objects")(parse("""{"o": {"s": "abc", "i": 123, "f": 123.4, "n": null}}""") must equal(withNestedObjects))
  test("parse with nested arrays")(parse("""{"a": ["abc", 123, 123.4, null]}""") must equal(withNestedArrays))

  // Generation
  test("generate empty")(parse("{}") must equal(pc(PObject())))
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
    new PObject(
      mutable.SortedMap(
        "s" -> IdmlValue("abc"),
        "i" -> IdmlValue(123),
        "f" -> IdmlValue(123.4),
        "n" -> IdmlNull
      ))

  def withNestedObjects =
    new PObject(
      mutable.SortedMap(
        "o" -> new PObject(
          mutable.SortedMap(
            "s" -> IdmlValue("abc"),
            "i" -> IdmlValue(123),
            "f" -> IdmlValue(123.4),
            "n" -> IdmlNull
          ))))

  def withNestedArrays =
    new PObject(
      mutable.SortedMap(
        "a" -> new PArray(
          mutable.ListBuffer(
            IdmlValue("abc"),
            IdmlValue(123),
            IdmlValue(123.4),
            IdmlNull
          ))))
}
