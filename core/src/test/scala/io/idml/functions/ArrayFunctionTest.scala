package io.idml.functions

import io.idml.datanodes.{PArray, PObject}
import io.idml.{InvalidCaller, PtolemyContext, PtolemyValue}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FunSuite, MustMatchers}

/**  */
class ArrayFunctionTest extends FunSuite with MustMatchers with MockitoSugar {

  val sizeTwoArray = PArray(PObject(), PObject())

  test("Cursor becomes InvalidCaller if the original cursor wasn't an array") {
    val ctx   = mock[PtolemyContext]
    val inner = mock[ApplyFunction]
    when(ctx.cursor).thenReturn(PtolemyValue(10))
    ArrayFunction(inner).invoke(ctx)
    verify(ctx).cursor_=(InvalidCaller)
  }

  test("Cursor becomes an empty array if the original cursor was an array") {
    val ctx   = mock[PtolemyContext]
    val inner = mock[ApplyFunction]
    when(ctx.cursor).thenReturn(PArray())
    ArrayFunction(inner).invoke(ctx)
    verify(ctx).cursor_=(PArray())
  }

  test("Cursor becomes a size-two array if the original cursor was size two") {
    pending
    val ctx   = mock[PtolemyContext]
    val inner = mock[ApplyFunction]
    when(ctx.cursor).thenReturn(sizeTwoArray)
    when(ctx.output).thenReturn(PObject())
    ArrayFunction(inner).invoke(ctx)
    verify(ctx, times(2)).cursor_=(PObject())
    verify(ctx).cursor_=(sizeTwoArray)
  }

  test("The wrapped block is invoked for each item in the cursor array") {
    val ctx   = mock[PtolemyContext]
    val inner = mock[ApplyFunction]
    when(ctx.cursor).thenReturn(sizeTwoArray)
    when(ctx.output).thenReturn(PObject())
    ArrayFunction(inner).invoke(ctx)
    verify(inner, times(2)).invoke(ctx)
  }

  test("The original output is preserved") {
    val ctx      = mock[PtolemyContext]
    val inner    = mock[ApplyFunction]
    val expected = ctx.output
    when(ctx.cursor).thenReturn(PArray.empty)
    ArrayFunction(inner).invoke(ctx)
    verify(ctx).output_=(expected)
  }

  test("The original scope is preserved") {
    pending
  }
}
