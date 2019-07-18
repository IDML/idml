package io.idml

import io.idml.lang.DocumentParseException
import io.idml.ast._
import org.mockito.Answers
import org.scalatest.FunSuite
import org.scalatest.mock.MockitoSugar

class IdmlParserTest extends FunSuite with MockitoSugar {

  test("Parses text") {
    new IdmlParser().parse(null, "a = b").nodes == Document(
      Map("main" -> Block("main", List(Assignment(List("a"), Pipeline(List(ExecNavRelative, Field("b"))))))))
  }

  test("Throws parse error when input is invalid") {
    intercept[DocumentParseException](new IdmlParser().parse(null, ":-D"))
  }
}
