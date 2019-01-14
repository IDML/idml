package io.idml

import org.scalatest.FunSuite

class FieldTypeCounterTest extends FunSuite {

  implicit def str2json(str: String): PtolemyValue = {
    PtolemyJson.parse(str)
  }

  test("counts top-level bools") {
    val fc = FieldTypeCounter()
    fc.visitValue("true")
    assert(fc === FieldTypeCounter(bools = 1))
  }

  test("counts top-level ints") {
    val fc = FieldTypeCounter()
    fc.visitValue("123")
    assert(fc === FieldTypeCounter(ints = 1))
  }

  test("counts top-level doubles") {
    val fc = FieldTypeCounter()
    fc.visitValue("123.0")
    assert(fc === FieldTypeCounter(doubles = 1))
  }

  test("counts top-level strings") {
    val fc = new FieldTypeCounter()
    fc.visitValue("\"abc\"")
    assert(fc === FieldTypeCounter(strings = 1))
  }

  test("counts top-level nulls") {
    val fc = new FieldTypeCounter()
    fc.visitValue("null")
    assert(fc === FieldTypeCounter(nulls = 1))
  }

  test("counts top-level objects") {
    val fc = new FieldTypeCounter()
    fc.visitValue("{}")
    assert(fc === FieldTypeCounter(objects = 1))
  }

  test("counts top-level arrays") {
    val fc = new FieldTypeCounter()
    fc.visitValue("[]")
    assert(fc === FieldTypeCounter(arrays = 1))
  }

  test("counts values nested in arrays") {
    val fc = new FieldTypeCounter()
    fc.visitValue("[{}, true, [\"a\", 1234]]")
    assert(fc === FieldTypeCounter(arrays = 2, bools = 1, objects = 1, strings = 1, ints = 1))
  }

  test("counts values nested in objects") {
    val fc = new FieldTypeCounter()
    fc.visitValue("{\"a\": \"a\", \"b\": true, \"c\": {\"d\": [], \"e\": 1234}}")
    assert(fc === FieldTypeCounter(arrays = 1, strings = 1, bools = 1, objects = 2, ints = 1))
  }
}
