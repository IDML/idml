package io.idml.datanodes

import io.idml.{PtolemyJson, PtolemyNull, PtolemyValue}
import org.scalatest._
import scala.collection.mutable

/** Test the behaviour of the PObject class */
class PObjectTest extends FunSuite with MustMatchers {
  import PtolemyJson._

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

  // Equality
  test("empty == empty")(empty must equal(empty))
  test("primitives == primitives")(withPrimitives must equal(withPrimitives))
  test("nested objects == nested objects")(withNestedObjects must equal(withNestedObjects))
  test("nested arrays == nested arrays")(withNestedArrays must equal(withNestedArrays))

  test("empty != primitives")(parse("{}") must not equal withPrimitives)
  test("primitives != nested arrays")(withPrimitives must not equal withNestedArrays)
  test("nested objects != nested arrays")(withNestedObjects must not equal withNestedArrays)
  test("nested arrays != nested objects")(withNestedArrays must not equal withNestedObjects)

  // serialize
  test("serialize should serialize")(PObject("test" -> PInt(2)).serialize() must equal(PString("""{"test":2}""")))

  // parse json
  test("parse json")(PString("{\"key\":\"value\"}\"}").parseJson() must equal(PObject("key" -> PString("value"))))
  test("parse a json number")(PString("4").parseJson() must equal(PInt(4)))
  test("parse json string")(PString("\"hello\"").parseJson() must equal(PString("hello")))

  // Test data

  def pc = compact _ andThen parse

  def withPrimitives =
    new PObject(
      mutable.Map(
        "s" -> PtolemyValue("abc"),
        "i" -> PtolemyValue(123),
        "f" -> PtolemyValue(123.4),
        "n" -> PtolemyNull
      ))

  def withNestedObjects =
    new PObject(
      mutable.Map(
        "o" -> new PObject(
          mutable.Map(
            "s" -> PtolemyValue("abc"),
            "i" -> PtolemyValue(123),
            "f" -> PtolemyValue(123.4),
            "n" -> PtolemyNull
          ))))

  def withNestedArrays =
    new PObject(
      mutable.Map(
        "a" -> new PArray(
          mutable.ListBuffer(
            PtolemyValue("abc"),
            PtolemyValue(123),
            PtolemyValue(123.4),
            PtolemyNull
          ))))

  // deepmerge
  val inner1 = PObject("b" -> PInt(2))
  val inner2 = PObject("c" -> PInt(3))
  val outer1 = PObject("a" -> inner1)
  val outer2 = PObject("a" -> inner2)

  test("deepmerge should merge objects")(outer1.deepMerge(outer2) must equal(PObject("a" -> PObject("b" -> PInt(2), "c" -> PInt(3)))))

  val one = PInt(1)
  val two = PInt(2)
  val x   = (p: PtolemyValue) => PObject("x" -> p)
  test("deepmerge should merge arrays") {
    x(PArray(one, two, one)) deepMerge x(PArray(two, two, one)) must equal(x(PArray(two, two, one)))
  }
  test("deepmerge should merge arrays where left is longer") {
    x(PArray(one, two, one, two)) deepMerge x(PArray(two, two, one)) must equal(x(PArray(two, two, one)))
  }
  test("deepmerge should merge arrays where right is longer") {
    x(PArray(one, two, one)) deepMerge x(PArray(two, two, one, one)) must equal(x(PArray(two, two, one, one)))
  }
  test("deepmerge should merge arrays with objects in") {
    x(PArray(x(one), x(two), x(one))) deepMerge x(PArray(x(two), x(two), x(one))) must equal(x(PArray(x(two), x(two), x(one))))
  }
}
