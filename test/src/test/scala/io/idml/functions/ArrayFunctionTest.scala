package io.idml.functions

import io.idml.datanodes.{IArray, IObject}
import io.idml.{IdmlContext, IdmlValue, InvalidCaller}
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

/** */
class ArrayFunctionTest extends AnyFunSuite with Matchers with MockitoSugar {

  val sizeTwoArray = IArray(IObject(), IObject())

  test("Cursor becomes InvalidCaller if the original cursor wasn't an array") {
    val ctx   = mock[IdmlContext]
    val inner = mock[ApplyFunction]
    when(ctx.cursor).thenReturn(IdmlValue(10))
    ArrayFunction(inner).invoke(ctx)
    verify(ctx).cursor_=(InvalidCaller)
  }

  test("Cursor becomes an empty array if the original cursor was an array") {
    val ctx   = mock[IdmlContext]
    val inner = mock[ApplyFunction]
    when(ctx.cursor).thenReturn(IArray())
    ArrayFunction(inner).invoke(ctx)
    verify(ctx).cursor_=(IArray())
  }

  test("Cursor becomes a size-two array if the original cursor was size two") {
    pending
    val ctx   = mock[IdmlContext]
    val inner = mock[ApplyFunction]
    when(ctx.cursor).thenReturn(sizeTwoArray)
    when(ctx.output).thenReturn(IObject())
    ArrayFunction(inner).invoke(ctx)
    verify(ctx, times(2)).cursor_=(IObject())
    verify(ctx).cursor_=(sizeTwoArray)
  }

  test("The wrapped block is invoked for each item in the cursor array") {
    val ctx   = mock[IdmlContext]
    val inner = mock[ApplyFunction]
    when(ctx.cursor).thenReturn(sizeTwoArray)
    when(ctx.output).thenReturn(IObject())
    ArrayFunction(inner).invoke(ctx)
    verify(inner, times(2)).invoke(ctx)
  }

  test("The original output is preserved") {
    val ctx      = mock[IdmlContext]
    val inner    = mock[ApplyFunction]
    val expected = ctx.output
    when(ctx.cursor).thenReturn(IArray.empty)
    ArrayFunction(inner).invoke(ctx)
    verify(ctx).output_=(expected)
  }

  test("The original scope is preserved") {
    pending
  }
}
