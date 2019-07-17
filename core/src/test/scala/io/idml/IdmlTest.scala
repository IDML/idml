package io.idml

import org.scalatest.FunSuite
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import scala.collection.JavaConverters._

class IdmlTest extends FunSuite with MockitoSugar {

  test("Passes strings to parser") {
    val parser = mock[IdmlParser]

    val ptolemy = new Idml(
      mock[IdmlConf],
      parser,
      mock[FileResolver],
      mock[ResourceResolver],
      mock[FunctionResolverService],
      List.empty[IdmlListener].asJava
    )

    ptolemy.fromString("abc")

    verify(parser).parse(ptolemy, "abc")
  }

  test("Resolves and parses files") {
    val fileResolver = mock[FileResolver]
    val parser       = mock[IdmlParser]

    when(fileResolver.resolveAndLoad("xyz")).thenReturn("abc")

    val ptolemy = new Idml(
      mock[IdmlConf],
      parser,
      fileResolver,
      mock[ResourceResolver],
      mock[FunctionResolverService],
      List.empty[IdmlListener].asJava
    )

    ptolemy.fromFile("xyz")

    verify(parser).parse(ptolemy, "abc")
  }

  test("Resolves and parses resources") {
    val resourceResolver = mock[ResourceResolver]
    val parser           = mock[IdmlParser]

    when(resourceResolver.resolveAndLoad("xyz")).thenReturn("abc")

    val ptolemy = new Idml(
      mock[IdmlConf],
      parser,
      mock[FileResolver],
      resourceResolver,
      mock[FunctionResolverService],
      List.empty[IdmlListener].asJava
    )

    ptolemy.fromResource("xyz")

    verify(parser).parse(ptolemy, "abc")
  }
}
