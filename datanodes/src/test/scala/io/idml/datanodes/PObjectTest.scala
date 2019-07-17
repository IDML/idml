package io.idml.datanodes

import io.idml.{IdmlNull, IdmlValue}
import org.scalatest._
import scala.collection.mutable

/** Test the behaviour of the PObject class */
class PObjectTest extends FunSuite with MustMatchers {

  // Equality
  test("empty == empty")(empty must equal(empty))
  test("primitives == primitives")(withPrimitives must equal(withPrimitives))
  test("nested objects == nested objects")(withNestedObjects must equal(withNestedObjects))
  test("nested arrays == nested arrays")(withNestedArrays must equal(withNestedArrays))

  test("empty != primitives")(PObject() must not equal withPrimitives)
  test("primitives != nested arrays")(withPrimitives must not equal withNestedArrays)
  test("nested objects != nested arrays")(withNestedObjects must not equal withNestedArrays)
  test("nested arrays != nested objects")(withNestedArrays must not equal withNestedObjects)

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

  // deepmerge
  val inner1 = PObject("b" -> PInt(2))
  val inner2 = PObject("c" -> PInt(3))
  val outer1 = PObject("a" -> inner1)
  val outer2 = PObject("a" -> inner2)

  test("deepmerge should merge objects")(outer1.deepMerge(outer2) must equal(PObject("a" -> PObject("b" -> PInt(2), "c" -> PInt(3)))))

  val one = PInt(1)
  val two = PInt(2)
  val x   = (p: IdmlValue) => PObject("x" -> p)
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
