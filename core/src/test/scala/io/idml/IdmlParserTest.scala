package io.idml

import io.idml.lang.DocumentParseException
import io.idml.ast._
import org.mockito.Answers
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.mockito.MockitoSugar

class IdmlParserTest extends AnyFunSuite with MockitoSugar {

  test("Parses text") {
    new IdmlParser().parse(null, "a = b").nodes == Document(
      Map("main" -> Block("main", List(Assignment(List("a"), Pipeline(List(ExecNavRelative, Field("b"))))))))
  }

  test("Throws parse error when input is invalid") {
    intercept[DocumentParseException](new IdmlParser().parse(null, ":-D"))
  }
}
