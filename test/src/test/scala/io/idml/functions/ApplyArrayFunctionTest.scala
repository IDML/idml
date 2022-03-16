package io.idml.functions

import io.idml.datanodes.{IArray, IInt, IObject}
import io.idml.ast.{Block, Document}
import io.idml.{IdmlContext, UnknownBlockException}
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

/** Verify the behaviour of the ApplyArrayFunction */
class ApplyArrayFunctionTest extends AnyFunSuite with Matchers with MockitoSugar {

  test("Constructor accepts a missing block") {
    ApplyArrayFunction("missing_block")
  }

  test("Block fails on invocation when given a missing block") {
    val ctx = mock[IdmlContext]
    when(ctx.doc).thenReturn(Document.empty)
    intercept[UnknownBlockException](ApplyArrayFunction("missing_block").invoke(ctx))
  }

  test("Invoke the named block if the block is present") {
    val doc   = mock[Document]
    val ctx   = mock[IdmlContext]
    val block = mock[Block]
    when(ctx.doc).thenReturn(doc)
    when(ctx.cursor).thenReturn(IArray())
    when(doc.blocks).thenReturn(Map("main" -> block))
    ApplyArrayFunction("main").invoke(ctx)
    verify(block).invoke(ctx)
  }

  test("Configure the context with a new output object") {
    val ctx = mock[IdmlContext]
    when(ctx.doc).thenReturn(Document.empty)
    when(ctx.cursor).thenReturn(IArray())
    ApplyArrayFunction("main").invoke(ctx)
    verify(ctx).output_=(IObject())
  }

  test("Preserve the original scope") {
    pending
  }

  test("Preserve the original output object") {
    val ctx      = mock[IdmlContext]
    val expected = ctx.output
    when(ctx.doc).thenReturn(Document.empty)
    ApplyArrayFunction("main").invoke(ctx)
    verify(ctx).output_=(expected)
  }

  test("Update the cursor with a new output object") {
    pending
  }

  test("Invoke the named block on each item in an array") {
    val doc   = mock[Document]
    val ctx   = mock[IdmlContext]
    val block = mock[Block]
    when(ctx.doc).thenReturn(doc)
    when(ctx.cursor).thenReturn(IArray(IInt(10), IInt(11), IInt(12)))
    when(doc.blocks).thenReturn(Map("main" -> block))
    when(ctx.output).thenReturn(IObject())
    ApplyArrayFunction("main").invoke(ctx)
    verify(ctx).cursor_=(IObject())
    verify(ctx, times(2)).output_=(IObject())
    verify(block).invoke(ctx)
  }

}
