package io.idml.ast

import io.idml.datanodes._
import io.idml.{IdmlContext, IdmlListener, IdmlValue, NoFields}
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.mockito.MockitoSugar

class AssignmentTest extends AnyFunSuite with MockitoSugar {

  def updateCursor(value: IdmlValue): Answer[Unit] = new Answer[Unit] {
    override def answer(invocation: InvocationOnMock) = {
      invocation.getArguments()(0).asInstanceOf[IdmlContext].cursor = value
    }
  }

  test("Invokes right hand side expression") {
    val pipl   = mock[Pipeline]
    val assign = new Assignment(List("a"), pipl)
    val ctx    = new IdmlContext()
    assign.invoke(ctx)
    verify(pipl).invoke(ctx)
  }

  test("Updates the output if the pipl updates the cursor to a value") {
    val pipl   = mock[Pipeline]
    val assign = new Assignment(List("a"), pipl)
    val ctx    = new IdmlContext()
    when(pipl.invoke(ctx)).thenAnswer(updateCursor(ITrue))
    assign.invoke(ctx)
    assert(ctx.output === IObject("a" -> ITrue))
  }

  test("Need to move assignments into a new class and have a test for nested updates") {
    pending
  }

  test("Need to move assignments into a new class and have a test for overwriting old values") {
    pending
  }

  test("Doesn't update the output if nothing is returned") {
    val pipl   = mock[Pipeline]
    val assign = new Assignment(List("a"), pipl)
    val ctx    = new IdmlContext()
    when(pipl.invoke(ctx)).thenAnswer(updateCursor(NoFields))
    assign.invoke(ctx)
    assert(ctx.output === IObject())
  }

  test("Triggers enter and exit events") {
    val pipl     = mock[Pipeline]
    val assign   = new Assignment(List("a"), pipl)
    val ctx      = new IdmlContext()
    val listener = mock[IdmlListener]
    ctx.listeners = List(listener)
    assign.invoke(ctx)
    verify(listener).enterAssignment(ctx, assign)
    verify(listener).exitAssignment(ctx, assign)
  }
}
