package io.idml

import org.scalatest.FunSuite
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import scala.collection.JavaConverters._

class PtolemyTest extends FunSuite with MockitoSugar {

  test("Passes strings to parser") {
    val parser = mock[PtolemyParser]

    val ptolemy = new Ptolemy(
      mock[PtolemyConf],
      parser,
      mock[FileResolver],
      mock[ResourceResolver],
      mock[FunctionResolverService],
      List.empty[PtolemyListener].asJava
    )

    ptolemy.fromString("abc")

    verify(parser).parse(ptolemy, "abc")
  }

  test("Resolves and parses files") {
    val fileResolver = mock[FileResolver]
    val parser       = mock[PtolemyParser]

    when(fileResolver.resolveAndLoad("xyz")).thenReturn("abc")

    val ptolemy = new Ptolemy(
      mock[PtolemyConf],
      parser,
      fileResolver,
      mock[ResourceResolver],
      mock[FunctionResolverService],
      List.empty[PtolemyListener].asJava
    )

    ptolemy.fromFile("xyz")

    verify(parser).parse(ptolemy, "abc")
  }

  test("Resolves and parses resources") {
    val resourceResolver = mock[ResourceResolver]
    val parser           = mock[PtolemyParser]

    when(resourceResolver.resolveAndLoad("xyz")).thenReturn("abc")

    val ptolemy = new Ptolemy(
      mock[PtolemyConf],
      parser,
      mock[FileResolver],
      resourceResolver,
      mock[FunctionResolverService],
      List.empty[PtolemyListener].asJava
    )

    ptolemy.fromResource("xyz")

    verify(parser).parse(ptolemy, "abc")
  }
}
