package io.idml

import io.idml.lang.DocumentParseException
import io.idml.ast._
import org.mockito.Answers
import org.scalatest.FunSuite
import org.scalatest.mock.MockitoSugar

class PtolemyParserTest extends FunSuite with MockitoSugar {

  test("Parses text") {
    val engine = mock[Ptolemy](Answers.RETURNS_DEEP_STUBS.get())
    new PtolemyParser().parse(engine, "a = b").nodes == Document(
      Map("main" -> Block("main", List(Assignment(List("a"), Pipeline(List(ExecNavRelative, Field("b"))))))))
  }

  test("Throws parse error when input is invalid") {
    val engine = mock[Ptolemy](Answers.RETURNS_DEEP_STUBS.get())
    intercept[DocumentParseException](new PtolemyParser().parse(engine, ":-D"))
  }
}
