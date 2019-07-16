package io.idml

import io.idml.ast.AstGenerator
import io.idml.datanodes.PString
import org.scalatest.{FunSuite, MustMatchers}

class AstString extends FunSuite with MustMatchers {
  val ast = new AstGenerator(new FunctionResolverService)

  test("string literals") {
    ast.decodeString("'hello world'") must equal(PString("hello world"))
    ast.decodeString("\"hello world\"") must equal(PString("hello world"))
    ast.decodeString("\"\"\"hello world\"\"\"") must equal(PString("hello world"))
  }

  test("escaping of newlines") {
    ast.decodeString("\"foo\\nbar\"") must equal(PString("foo\nbar"))
    ast.decodeString("'foo\\nbar'") must equal(PString("foo\nbar"))
    ast.decodeString("\"\"\"foo\\nbar\"\"\"") must equal(PString("foo\\nbar"))
  }

  test("unicode escapes") {
    ast.decodeString("\"\\u0027\"") must equal(PString("'"))
    ast.decodeString("'\\u0027'") must equal(PString("'"))
    ast.decodeString("\"\"\"\\u0027\"\"\"") must equal(PString("'"))
  }

  test("using safe termination characters in the other types") {
    ast.decodeString("\"'hello world'\"") must equal(PString("'hello world'"))
    ast.decodeString("'hello\"world'") must equal(PString("hello\"world"))
    ast.decodeString("\"\"\"'hello\"world'\"\"\"") must equal(PString("'hello\"world'"))
  }

  test("escaping the current string terminator") {
    ast.decodeString("""'hello\'world'""") must equal(PString("""hello'world"""))
    ast.decodeString("\"hello\\\"world\"") must equal(PString("""hello"world"""))
    ast.decodeString("\"\"\"hello\\\"\\\"\\\"world\"\"\"") must equal(PString("""hello\"\"\"world"""))
  }
}
