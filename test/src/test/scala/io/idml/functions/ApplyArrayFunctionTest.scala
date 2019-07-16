package io.idml.functions

import io.idml.datanodes.{PArray, PInt, PObject}
import io.idml.ast.{Block, Document}
import io.idml.{PtolemyContext, UnknownBlockException}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FunSuite, MustMatchers}

/** Verify the behaviour of the ApplyArrayFunction */
class ApplyArrayFunctionTest extends FunSuite with MustMatchers with MockitoSugar {

  test("Constructor accepts a missing block") {
    ApplyArrayFunction("missing_block")
  }

  test("Block fails on invocation when given a missing block") {
    val ctx = mock[PtolemyContext]
    when(ctx.doc).thenReturn(Document.empty)
    intercept[UnknownBlockException](ApplyArrayFunction("missing_block").invoke(ctx))
  }

  test("Invoke the named block if the block is present") {
    val doc   = mock[Document]
    val ctx   = mock[PtolemyContext]
    val block = mock[Block]
    when(ctx.doc).thenReturn(doc)
    when(ctx.cursor).thenReturn(PArray())
    when(doc.blocks).thenReturn(Map("main" -> block))
    ApplyArrayFunction("main").invoke(ctx)
    verify(block).invoke(ctx)
  }

  test("Configure the context with a new output object") {
    val ctx = mock[PtolemyContext]
    when(ctx.doc).thenReturn(Document.empty)
    when(ctx.cursor).thenReturn(PArray())
    ApplyArrayFunction("main").invoke(ctx)
    verify(ctx).output_=(PObject())
  }

  test("Preserve the original scope") {
    pending
  }

  test("Preserve the original output object") {
    val ctx      = mock[PtolemyContext]
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
    val ctx   = mock[PtolemyContext]
    val block = mock[Block]
    when(ctx.doc).thenReturn(doc)
    when(ctx.cursor).thenReturn(PArray(PInt(10), PInt(11), PInt(12)))
    when(doc.blocks).thenReturn(Map("main" -> block))
    when(ctx.output).thenReturn(PObject())
    ApplyArrayFunction("main").invoke(ctx)
    verify(ctx).cursor_=(PObject())
    verify(ctx, times(2)).output_=(PObject())
    verify(block).invoke(ctx)
  }

}
