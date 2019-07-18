package io.idml.jackson

import io.idml.{IdmlNull, IdmlValue}
import io.idml.datanodes.{IArray, IObject}
import org.scalatest.{FunSuite, MustMatchers}

import scala.collection.mutable

class ArrayParsingSpec extends FunSuite with MustMatchers {
  import IdmlJackson.default._

  // parsing
  test("parse empty")(parse("[]") must equal(IArray()))
  test("parse with primitives")(parse("""["abc", 123, 123.4, null]""") must equal(withPrimitives))
  test("parse with nested objects")(parse("""[{"s": "abc", "i": 123, "f": 123.4, "n": null}]""") must equal(withNestedObjects))
  test("parse with nested arrays")(parse("""[["abc", 123, 123.4, null]]""") must equal(withNestedArrays))

  // Generation
  test("generate empty")(parse("[]") must equal(pc(IArray())))
  test("generate with primitives")(parse("""["abc", 123, 123.4, null]""") must equal(pc(withPrimitives)))
  test("generate with nested objects")(parse("""[{"s": "abc", "i": 123, "f": 123.4, "n": null}]""") must equal(pc(withNestedObjects)))
  test("generate with nested arrays")(parse("""[["abc", 123, 123.4, null]]""") must equal(pc(withNestedArrays)))

  def pc = compact _ andThen parse

  def withPrimitives =
    new IArray(
      mutable.ListBuffer(
        IdmlValue("abc"),
        IdmlValue(123),
        IdmlValue(123.4),
        IdmlNull
      ))

  def withNestedObjects =
    new IArray(
      mutable.ListBuffer(
        new IObject(
          mutable.SortedMap(
            "s" -> IdmlValue("abc"),
            "i" -> IdmlValue(123),
            "f" -> IdmlValue(123.4),
            "n" -> IdmlNull
          ))))

  def withNestedArrays = new IArray(mutable.ListBuffer(withPrimitives))
}
