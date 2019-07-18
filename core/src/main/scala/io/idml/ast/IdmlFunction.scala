package io.idml.ast

import io.idml.datanodes.{IArray, IObject}
import io.idml.{IdmlArray, IdmlContext, IdmlValue, UnknownBlockException}

import scala.collection.mutable

/** The base class for functions invoked in the mapping language, e.g. x.int() */
trait IdmlFunction extends Expression {
  def name: String
  def args: List[Pipeline]

  /** Create arg values */
  protected def execArgs(ctx: IdmlContext): Seq[IdmlValue] = {
    args.map(_.eval(ctx))
  }

  /** Resolve the block in the document or fail */
  protected def findBlock(ctx: IdmlContext, block: String) = {
    // TODO: The 'late binding' approach adds slight overhead. Wiring the block up after the full document has been
    // parsed will eliminate the additional hashmap calls
    ctx.doc.blocks.getOrElse(block, throw new UnknownBlockException(s"Section '$block' is not defined"))
  }

  /** Apply a block to each item in an array */
  protected def applyBlockToArray(ctx: IdmlContext, block: Node, array: IdmlArray): Unit = {
    val results: mutable.Buffer[IdmlValue] = array.items.map { item =>
      ctx.scope = item
      ctx.cursor = item
      ctx.output = IObject()
      block.invoke(ctx)
      ctx.output
    }
    ctx.cursor = IArray(results)
  }

  /** Apply a block to a single item */
  protected def applyBlock(ctx: IdmlContext, block: Node, other: IdmlValue): Unit = {
    // Prepare the context to call the block in isolation
    ctx.scope = ctx.cursor
    ctx.output = IObject()
    block.invoke(ctx)
    ctx.cursor = ctx.output
  }
}

case class IdmlFunctionMetadata(
    name: String,
    arguments: List[(String, String)],
    description: String
)
