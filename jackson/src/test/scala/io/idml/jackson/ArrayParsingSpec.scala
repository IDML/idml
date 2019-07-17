package io.idml.jackson

import io.idml.{PtolemyNull, PtolemyValue}
import io.idml.datanodes.{PArray, PObject}
import org.scalatest.{FunSuite, MustMatchers}

import scala.collection.mutable

class ArrayParsingSpec extends FunSuite with MustMatchers {
  import PtolemyJackson.default._

  // parsing
  test("parse empty")(parse("[]") must equal(PArray()))
  test("parse with primitives")(parse("""["abc", 123, 123.4, null]""") must equal(withPrimitives))
  test("parse with nested objects")(parse("""[{"s": "abc", "i": 123, "f": 123.4, "n": null}]""") must equal(withNestedObjects))
  test("parse with nested arrays")(parse("""[["abc", 123, 123.4, null]]""") must equal(withNestedArrays))

  // Generation
  test("generate empty")(parse("[]") must equal(pc(PArray())))
  test("generate with primitives")(parse("""["abc", 123, 123.4, null]""") must equal(pc(withPrimitives)))
  test("generate with nested objects")(parse("""[{"s": "abc", "i": 123, "f": 123.4, "n": null}]""") must equal(pc(withNestedObjects)))
  test("generate with nested arrays")(parse("""[["abc", 123, 123.4, null]]""") must equal(pc(withNestedArrays)))

  def pc = compact _ andThen parse

  def withPrimitives =
    new PArray(
      mutable.ListBuffer(
        PtolemyValue("abc"),
        PtolemyValue(123),
        PtolemyValue(123.4),
        PtolemyNull
      ))

  def withNestedObjects =
    new PArray(
      mutable.ListBuffer(
        new PObject(
          mutable.SortedMap(
            "s" -> PtolemyValue("abc"),
            "i" -> PtolemyValue(123),
            "f" -> PtolemyValue(123.4),
            "n" -> PtolemyNull
          ))))

  def withNestedArrays = new PArray(mutable.ListBuffer(withPrimitives))
}
