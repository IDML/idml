package io.idml

import io.idml.datanodes.{IInt, IObject, IString}
import org.mockito.Answers
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.{FunSuite, MustMatchers}
import org.scalatest.mock.MockitoSugar

import scala.collection.JavaConverters._

class IdmlChainTest extends FunSuite with MockitoSugar with MustMatchers {

  test("Can't create an empty chain") {
    intercept[IllegalArgumentException](new IdmlChain(mock[Idml]))
  }

  test("Enter and exit chain methods are called") {
    val ptolemy  = mock[Idml](Answers.RETURNS_DEEP_STUBS.get())
    val first    = mock[IdmlMapping]
    val ctx      = mock[IdmlContext]
    val listener = mock[IdmlListener]

    when(ptolemy.listeners).thenReturn(List(listener).asJava)

    val chain = new IdmlChain(ptolemy, first)
    chain.run(ctx)

    verify(ctx).enterChain()
    verify(ctx).exitChain()
  }

  test("Mappings have the run() method called") {
    val ptolemy  = mock[Idml](Answers.RETURNS_DEEP_STUBS.get())
    val first    = mock[IdmlMapping]
    val second   = mock[IdmlMapping]
    val ctx      = mock[IdmlContext]
    val listener = mock[IdmlListener]

    when(ptolemy.listeners).thenReturn(List(listener).asJava)

    val chain = new IdmlChain(ptolemy, first, second)
    chain.run(ctx)

    verify(first).run(ctx)
    verify(second).run(ctx)
  }

  test("chains run in order, operating on the same object") {
    val ptolemy = new Idml()
    val chain   = ptolemy.newChain(List("a = initial + 1", "b = a + 1", "c = b + 1").map(ptolemy.fromString): _*)
    chain.run(IObject("initial" -> IInt(0))) must equal(IObject("a" -> IInt(1), "b" -> IInt(2), "c" -> IInt(3)))
  }

}
