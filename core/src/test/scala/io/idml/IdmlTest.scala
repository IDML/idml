package io.idml

import org.scalatest.FunSuite
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import scala.collection.JavaConverters._

class IdmlTest extends FunSuite with MockitoSugar {

  test("Passes strings to parser") {
    val parser = mock[IdmlParser]

    val funcs = mock[FunctionResolverService]
    val ptolemy = new Idml(
      parser,
      funcs,
      List.empty[IdmlListener].asJava
    )

    ptolemy.compile("abc")

    verify(parser).parse(funcs, "abc")
  }

}
