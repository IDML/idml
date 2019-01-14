package io.idml

import io.idml.datanodes.{PInt, PObject, PString}
import org.mockito.Answers
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.{FunSuite, MustMatchers}
import org.scalatest.mock.MockitoSugar

import scala.collection.JavaConverters._

class PtolemyChainTest extends FunSuite with MockitoSugar with MustMatchers {

  test("Can't create an empty chain") {
    intercept[IllegalArgumentException](new PtolemyChain(mock[Ptolemy]))
  }

  test("Enter and exit chain methods are called") {
    val ptolemy  = mock[Ptolemy](Answers.RETURNS_DEEP_STUBS.get())
    val first    = mock[PtolemyMapping]
    val ctx      = mock[PtolemyContext]
    val listener = mock[PtolemyListener]

    when(ptolemy.listeners).thenReturn(List(listener).asJava)

    val chain = new PtolemyChain(ptolemy, first)
    chain.run(ctx)

    verify(ctx).enterChain()
    verify(ctx).exitChain()
  }

  test("Mappings have the run() method called") {
    val ptolemy  = mock[Ptolemy](Answers.RETURNS_DEEP_STUBS.get())
    val first    = mock[PtolemyMapping]
    val second   = mock[PtolemyMapping]
    val ctx      = mock[PtolemyContext]
    val listener = mock[PtolemyListener]

    when(ptolemy.listeners).thenReturn(List(listener).asJava)

    val chain = new PtolemyChain(ptolemy, first, second)
    chain.run(ctx)

    verify(first).run(ctx)
    verify(second).run(ctx)
  }

  test("chains run in order, operating on the same object") {
    val ptolemy = new Ptolemy()
    val chain   = ptolemy.newChain(List("a = initial + 1", "b = a + 1", "c = b + 1").map(ptolemy.fromString): _*)
    chain.run(PObject("initial" -> PInt(0))) must equal(PObject("a" -> PInt(1), "b" -> PInt(2), "c" -> PInt(3)))
  }

}
