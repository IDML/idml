package io.idml.jackson.difftool

import io.idml.datanodes._
import io.idml._
import io.idml.jackson.IdmlJackson
import org.scalatest.funsuite.AnyFunSuite

class DiffTest extends AnyFunSuite {

  /** Used to simplify tests */
  implicit def strToJson(str: String): IdmlValue = IdmlJackson.default.parse(str)

  /** Used to simplify tests */
  def diff(left: IdmlValue, right: IdmlValue): IdmlValue =
    Diff.createDiff(left, right)

  /** Used to simplify tests */
  def check(left: IdmlValue, right: IdmlValue, expected: IdmlValue): Unit = {
    val result = Diff.compare(left, right)
    assert(result === expected)
    // uncomment me to generate examples
    // println(Diff.pretty(result))
  }

  test("isDiff and createDiff relationship - isDiff(createDiff(x)) == true") {
    assert(Diff.isDiff(Diff.createDiff(ITrue, IFalse)))
  }

  test("isDiff and createDiff relationship - isDiff(other) == false") {
    assert(Diff.isDiff(ITrue) === false)
  }

  test("primitives - true v false") {
    check("true", "false", diff("true", "false"))
  }

  test("primitives - true v true") {
    check("true", "true", "true")
  }

  test("primitives - int v string") {
    check("123", "'abc'", diff("123", "'abc'"))
  }

  test("primitives - null v bool") {
    check("null", "true", diff("null", "true"))
  }

  test("objects and primitives - object v bool") {
    check("{abc: false}", "false", diff("{abc: false}", "false"))
  }

  test("objects and primitives - bool v object") {
    check("false", "{abc: false}", diff("false", "{abc: false}"))
  }

  test("objects and objects - {} vs {}") {
    check("{}", "{}", "{}")
  }

  test("objects and objects - {x: a} vs {}") {
    val left     = "{x: 'a'}"
    val right    = "{}"
    val expected = IObject("x" -> diff("'a'", MissingField))
    check(left, right, expected)
  }

  test("objects and objects - {} vs {x: a}") {
    val left     = "{}"
    val right    = "{x: 'a'}"
    val expected = IObject("x" -> diff(MissingField, "'a'"))
    check(left, right, expected)
  }

  test("objects and objects - {x: a} vs {x: b}") {
    val left     = "{x: 'a'}"
    val right    = "{x: 'b'}"
    val expected = IObject("x" -> diff("'a'", "'b'"))
    check(left, right, expected)
  }

  test("nested objects - {x: {y: A}} vs {x: {y: B}") {
    val left     = "{x: {y: 'a'}}"
    val right    = "{x: {y: 'b'}}"
    val expected = IObject("x" -> IObject("y" -> diff("'a'", "'b'")))
    check(left, right, expected)
  }

  test(
    "nested objects - {x: {y: A}} vs {x: {z: B}} == {x: {y: [__DIFF__, A, _], z: [__DIFF__, _, B}}") {
    val left     = "{x: {y: 'a'}}"
    val right    = "{x: {z: 'b'}}"
    val expected =
      IObject("x" -> IObject("y" -> diff("'a'", MissingField), "z" -> diff(MissingField, "'b'")))
    check(left, right, expected)
  }

  test("nested objects - {x: {y: A, z: B}} vs {x: {z: B}} == {x: {y: [__DIFF__, A, _], z: B}") {
    val left     = "{x: {y: 'a', z: 'b'}}"
    val right    = "{x: {z: 'b'}}"
    val expected =
      IObject("x" -> IObject("y" -> diff("'a'", MissingField), "z" -> "'b'"))
    check(left, right, expected)
  }

  test("arrays and primitives - [false] v false") {
    check("[false]", "false", diff("[false]", "false"))
  }

  test("arrays and primitives - false v [false]") {
    check("false", "[false]", diff("false", "[false]"))
  }

  test("arrays and arrays - [] vs []") {
    check("[]", "[]", "[]")
  }

  test("arrays and arrays - [a] vs []") {
    val left     = "['a']"
    val right    = "[]"
    val expected = IArray(diff("'a'", MissingIndex))
    check(left, right, expected)
  }

  test("arrays and arrays - [] vs [a]") {
    val left     = IArray()
    val right    = IArray(IString("a"))
    val expected = IArray(diff(MissingIndex, IString("a")))
    check(left, right, expected)
  }

  test("arrays and arrays - [a] vs [b]") {
    val left     = IArray(IString("a"))
    val right    = IArray(IString("b"))
    val expected = IArray(diff(IString("a"), IString("b")))
    check(left, right, expected)
  }

  test("arrays and arrays - [a] vs [a, b]") {
    val left     = IArray(IString("a"))
    val right    = IArray(IString("a"), IString("b"))
    val expected = IArray(IString("a"), diff(MissingIndex, IString("b")))
    check(left, right, expected)
  }

  test("arrays and arrays - [a, b] vs [a]") {
    val left     = IArray(IString("a"), IString("b"))
    val right    = IArray(IString("a"))
    val expected = IArray(IString("a"), diff(IString("b"), MissingIndex))
    check(left, right, expected)
  }

  test("arrays and arrays - [a, b, c] vs [a, x, c]") {
    val left     = IArray(IString("a"), IString("b"), IString("c"))
    val right    = IArray(IString("a"), IString("x"), IString("c"))
    val expected =
      IArray(IString("a"), diff(IString("b"), IString("x")), IString("c"))
    check(left, right, expected)
  }

  test("nested arrays - [[a]] vs [[b]]") {
    val left     = IArray(IArray(IString("a")))
    val right    = IArray(IArray(IString("b")))
    val expected = IArray(IArray(diff(IString("a"), IString("b"))))
    check(left, right, expected)
  }

  test("nested arrays - [[a, b]] vs [[a]]") {
    val left     = IArray(IArray(IString("a"), IString("b")))
    val right    = IArray(IArray(IString("a")))
    val expected =
      IArray(IArray(IString("a"), diff(IString("b"), MissingIndex)))
    check(left, right, expected)
  }

  test("nested arrays - [[a]] vs [[a, b]]") {
    val left     = IArray(IArray(IString("a"), IString("b")))
    val right    = IArray(IArray(IString("a")))
    val expected =
      IArray(IArray(IString("a"), diff(IString("b"), MissingIndex)))
    check(left, right, expected)
  }

  test("nested arrays - [[a], [b]] vs [[a], [b, c]]") {
    val left     = IArray(IArray(IString("a")), IArray(IString("b")))
    val right    = IArray(IArray(IString("a")), IArray(IString("b"), IString("c")))
    val expected =
      IArray(IArray(IString("a")), IArray(IString("b"), diff(MissingIndex, IString("c"))))
    check(left, right, expected)
  }

  test("objects in arrays - [{x: a}] vs [{x: b}]") {
    val left     = IArray(IObject("x" -> IString("a")))
    val right    = IArray(IObject("x" -> IString("b")))
    val expected = IArray(IObject("x" -> diff(IString("a"), IString("b"))))
    check(left, right, expected)
  }

  test("arrays in objects - {x: [a]} vs {x: [b]}") {
    val left     = IObject("x" -> IArray(IString("a")))
    val right    = IObject("x" -> IArray(IString("b")))
    val expected = IObject("x" -> IArray(diff(IString("a"), IString("b"))))
    check(left, right, expected)
  }

  test("diff pretty printing - nested objects") {
    val left  = "{x: {y: 'a', z: 'b'}}"
    val right = "{x: {z: 'b'}}"
    assert(
      Diff.pretty(left, right) ===
        """{
        |  "x" : {
        |    "y"<removed> : "a"</removed>,
        |    "z" : "b"
        |  }
        |}""".stripMargin
    )
  }

}
