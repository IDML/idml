package io.idml

import io.idml.ast.AstGenerator
import io.idml.datanodes.IString
import org.scalatest.{FunSuite, MustMatchers}

class AstString extends FunSuite with MustMatchers {
  val ast = new AstGenerator(new FunctionResolverService)

  test("string literals") {
    ast.decodeString("'hello world'") must equal(IString("hello world"))
    ast.decodeString("\"hello world\"") must equal(IString("hello world"))
    ast.decodeString("\"\"\"hello world\"\"\"") must equal(IString("hello world"))
  }

  test("escaping of newlines") {
    ast.decodeString("\"foo\\nbar\"") must equal(IString("foo\nbar"))
    ast.decodeString("'foo\\nbar'") must equal(IString("foo\nbar"))
    ast.decodeString("\"\"\"foo\\nbar\"\"\"") must equal(IString("foo\\nbar"))
  }

  test("unicode escapes") {
    ast.decodeString("\"\\u0027\"") must equal(IString("'"))
    ast.decodeString("'\\u0027'") must equal(IString("'"))
    ast.decodeString("\"\"\"\\u0027\"\"\"") must equal(IString("'"))
  }

  test("using safe termination characters in the other types") {
    ast.decodeString("\"'hello world'\"") must equal(IString("'hello world'"))
    ast.decodeString("'hello\"world'") must equal(IString("hello\"world"))
    ast.decodeString("\"\"\"'hello\"world'\"\"\"") must equal(IString("'hello\"world'"))
  }

  test("escaping the current string terminator") {
    ast.decodeString("""'hello\'world'""") must equal(IString("""hello'world"""))
    ast.decodeString("\"hello\\\"world\"") must equal(IString("""hello"world"""))
    ast.decodeString("\"\"\"hello\\\"\\\"\\\"world\"\"\"") must equal(IString("""hello\"\"\"world"""))
  }
}
