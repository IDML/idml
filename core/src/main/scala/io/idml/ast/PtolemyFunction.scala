package io.idml.ast

import io.idml.datanodes.{PArray, PObject}
import io.idml.{PtolemyArray, PtolemyContext, PtolemyValue, UnknownBlockException}

import scala.collection.mutable

/** The base class for functions invoked in the mapping language, e.g. x.int() */
trait PtolemyFunction extends Expression {
  def name: String
  def args: List[Pipeline]

  /** Create arg values */
  protected def execArgs(ctx: PtolemyContext): Seq[PtolemyValue] = {
    args.map(_.eval(ctx))
  }

  /** Resolve the block in the document or fail */
  protected def findBlock(ctx: PtolemyContext, block: String) = {
    // TODO: The 'late binding' approach adds slight overhead. Wiring the block up after the full document has been
    // parsed will eliminate the additional hashmap calls
    ctx.doc.blocks.getOrElse(block, throw new UnknownBlockException(s"Section '$block' is not defined"))
  }

  /** Apply a block to each item in an array */
  protected def applyBlockToArray(ctx: PtolemyContext, block: Node, array: PtolemyArray): Unit = {
    val results: mutable.Buffer[PtolemyValue] = array.items.map { item =>
      ctx.scope = item
      ctx.cursor = item
      ctx.output = PObject()
      block.invoke(ctx)
      ctx.output
    }
    ctx.cursor = PArray(results)
  }

  /** Apply a block to a single item */
  protected def applyBlock(ctx: PtolemyContext, block: Node, other: PtolemyValue): Unit = {
    // Prepare the context to call the block in isolation
    ctx.scope = ctx.cursor
    ctx.output = PObject()
    block.invoke(ctx)
    ctx.cursor = ctx.output
  }
}

case class PtolemyFunctionMetadata(
    name: String,
    arguments: List[(String, String)],
    description: String
)
