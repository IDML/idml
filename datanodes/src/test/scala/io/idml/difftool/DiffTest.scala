package io.idml.difftool

import io.idml.datanodes._
import io.idml._
import org.scalatest.FunSuite

class DiffTest extends FunSuite {

  /** Used to simplify tests */
  implicit def strToJson(str: String): PtolemyValue = PtolemyJson.parse(str)

  /** Used to simplify tests */
  def diff(left: PtolemyValue, right: PtolemyValue): PtolemyValue =
    Diff.createDiff(left, right)

  /** Used to simplify tests */
  def check(left: PtolemyValue, right: PtolemyValue, expected: PtolemyValue): Unit = {
    val result = Diff.compare(left, right)
    assert(result === expected)
    // uncomment me to generate examples
    //println(Diff.pretty(result))
  }

  test("isDiff and createDiff relationship - isDiff(createDiff(x)) == true") {
    assert(Diff.isDiff(Diff.createDiff(PTrue, PFalse)))
  }

  test("isDiff and createDiff relationship - isDiff(other) == false") {
    assert(Diff.isDiff(PTrue) === false)
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
    val expected = PObject("x" -> diff("'a'", MissingField))
    check(left, right, expected)
  }

  test("objects and objects - {} vs {x: a}") {
    val left     = "{}"
    val right    = "{x: 'a'}"
    val expected = PObject("x" -> diff(MissingField, "'a'"))
    check(left, right, expected)
  }

  test("objects and objects - {x: a} vs {x: b}") {
    val left     = "{x: 'a'}"
    val right    = "{x: 'b'}"
    val expected = PObject("x" -> diff("'a'", "'b'"))
    check(left, right, expected)
  }

  test("nested objects - {x: {y: A}} vs {x: {y: B}") {
    val left     = "{x: {y: 'a'}}"
    val right    = "{x: {y: 'b'}}"
    val expected = PObject("x" -> PObject("y" -> diff("'a'", "'b'")))
    check(left, right, expected)
  }

  test("nested objects - {x: {y: A}} vs {x: {z: B}} == {x: {y: [__DIFF__, A, _], z: [__DIFF__, _, B}}") {
    val left     = "{x: {y: 'a'}}"
    val right    = "{x: {z: 'b'}}"
    val expected = PObject("x" -> PObject("y" -> diff("'a'", MissingField), "z" -> diff(MissingField, "'b'")))
    check(left, right, expected)
  }

  test("nested objects - {x: {y: A, z: B}} vs {x: {z: B}} == {x: {y: [__DIFF__, A, _], z: B}") {
    val left  = "{x: {y: 'a', z: 'b'}}"
    val right = "{x: {z: 'b'}}"
    val expected =
      PObject("x" -> PObject("y" -> diff("'a'", MissingField), "z" -> "'b'"))
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
    val expected = PArray(diff("'a'", MissingIndex))
    check(left, right, expected)
  }

  test("arrays and arrays - [] vs [a]") {
    val left     = PArray()
    val right    = PArray(PString("a"))
    val expected = PArray(diff(MissingIndex, PString("a")))
    check(left, right, expected)
  }

  test("arrays and arrays - [a] vs [b]") {
    val left     = PArray(PString("a"))
    val right    = PArray(PString("b"))
    val expected = PArray(diff(PString("a"), PString("b")))
    check(left, right, expected)
  }

  test("arrays and arrays - [a] vs [a, b]") {
    val left     = PArray(PString("a"))
    val right    = PArray(PString("a"), PString("b"))
    val expected = PArray(PString("a"), diff(MissingIndex, PString("b")))
    check(left, right, expected)
  }

  test("arrays and arrays - [a, b] vs [a]") {
    val left     = PArray(PString("a"), PString("b"))
    val right    = PArray(PString("a"))
    val expected = PArray(PString("a"), diff(PString("b"), MissingIndex))
    check(left, right, expected)
  }

  test("arrays and arrays - [a, b, c] vs [a, x, c]") {
    val left  = PArray(PString("a"), PString("b"), PString("c"))
    val right = PArray(PString("a"), PString("x"), PString("c"))
    val expected =
      PArray(PString("a"), diff(PString("b"), PString("x")), PString("c"))
    check(left, right, expected)
  }

  test("nested arrays - [[a]] vs [[b]]") {
    val left     = PArray(PArray(PString("a")))
    val right    = PArray(PArray(PString("b")))
    val expected = PArray(PArray(diff(PString("a"), PString("b"))))
    check(left, right, expected)
  }

  test("nested arrays - [[a, b]] vs [[a]]") {
    val left  = PArray(PArray(PString("a"), PString("b")))
    val right = PArray(PArray(PString("a")))
    val expected =
      PArray(PArray(PString("a"), diff(PString("b"), MissingIndex)))
    check(left, right, expected)
  }

  test("nested arrays - [[a]] vs [[a, b]]") {
    val left  = PArray(PArray(PString("a"), PString("b")))
    val right = PArray(PArray(PString("a")))
    val expected =
      PArray(PArray(PString("a"), diff(PString("b"), MissingIndex)))
    check(left, right, expected)
  }

  test("nested arrays - [[a], [b]] vs [[a], [b, c]]") {
    val left  = PArray(PArray(PString("a")), PArray(PString("b")))
    val right = PArray(PArray(PString("a")), PArray(PString("b"), PString("c")))
    val expected =
      PArray(PArray(PString("a")), PArray(PString("b"), diff(MissingIndex, PString("c"))))
    check(left, right, expected)
  }

  test("objects in arrays - [{x: a}] vs [{x: b}]") {
    val left     = PArray(PObject("x" -> PString("a")))
    val right    = PArray(PObject("x" -> PString("b")))
    val expected = PArray(PObject("x" -> diff(PString("a"), PString("b"))))
    check(left, right, expected)
  }

  test("arrays in objects - {x: [a]} vs {x: [b]}") {
    val left     = PObject("x" -> PArray(PString("a")))
    val right    = PObject("x" -> PArray(PString("b")))
    val expected = PObject("x" -> PArray(diff(PString("a"), PString("b"))))
    check(left, right, expected)
  }

  test("diff pretty printing - nested objects") {
    val left  = "{x: {y: 'a', z: 'b'}}"
    val right = "{x: {z: 'b'}}"
    assert(
      Diff.pretty(left, right) ===
        """{
        |  "x" : {
        |    "z" : "b",
        |    "y"<removed> : "a"</removed>
        |  }
        |}""".stripMargin)
  }

}
