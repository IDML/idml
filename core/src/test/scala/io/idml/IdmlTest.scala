package io.idml

import org.scalatest.funsuite.AnyFunSuite
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar

import scala.collection.JavaConverters._

class IdmlTest extends AnyFunSuite with MockitoSugar {

  test("Passes strings to parser") {
    val parser = mock[IdmlParser]

    val funcs   = mock[FunctionResolverService]
    val ptolemy = new Idml(
      parser,
      funcs,
      List.empty[IdmlListener].asJava
    )

    ptolemy.compile("abc")

    verify(parser).parse(funcs, "abc")
  }

}
