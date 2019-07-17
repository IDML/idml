package io.idml.functions

import io.idml.datanodes.{PArray, PInt, PObject}
import io.idml.ast.{Block, Document}
import io.idml.{IdmlContext, NoFields, UnknownBlockException}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FunSuite, MustMatchers}

/** Verify the behaviour of the ApplyFunction */
class ApplyFunctionTest extends FunSuite with MustMatchers with MockitoSugar {

  test("Constructor accepts a missing block") {
    ApplyFunction("missing_block")
  }

  test("Block fails on invocation when given a missing block") {
    val ctx = mock[IdmlContext]
    when(ctx.doc).thenReturn(Document.empty)
    intercept[UnknownBlockException](ApplyFunction("missing_block").invoke(ctx))
  }

  test("Don't call the function or touch the cursor if the current value is missing") {
    val doc   = mock[Document]
    val ctx   = mock[IdmlContext]
    val block = mock[Block]
    when(ctx.doc).thenReturn(doc)
    when(ctx.cursor).thenReturn(NoFields)
    when(doc.blocks).thenReturn(Map("main" -> block))
    ApplyFunction("main").invoke(ctx)
    verify(block, times(0)).invoke(ctx)
  }

  test("Invoke the named block if the block is present") {
    val doc   = mock[Document]
    val ctx   = mock[IdmlContext]
    val block = mock[Block]
    when(ctx.doc).thenReturn(doc)
    when(doc.blocks).thenReturn(Map("main" -> block))
    ApplyFunction("main").invoke(ctx)
    verify(block).invoke(ctx)
  }

  test("Configure the context with a new output object") {
    val ctx = mock[IdmlContext]
    when(ctx.doc).thenReturn(Document.empty)
    ApplyFunction("main").invoke(ctx)
    verify(ctx).output_=(PObject())
  }

  test("Preserve the original scope") {
    pending
  }

  test("Preserve the original output object") {
    val ctx      = mock[IdmlContext]
    val expected = ctx.output
    when(ctx.doc).thenReturn(Document.empty)
    ApplyFunction("main").invoke(ctx)
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
    when(ctx.cursor).thenReturn(PArray(PInt(10), PInt(11), PInt(12)))
    when(doc.blocks).thenReturn(Map("main" -> block))
    ApplyFunction("main").invoke(ctx)
    verify(ctx).cursor_=(PInt(10))
    verify(ctx).cursor_=(PInt(11))
    verify(ctx).cursor_=(PInt(12))
    verify(block, times(3)).invoke(ctx)
  }

}
