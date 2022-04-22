package io.idml.functions

import io.idml.datanodes.IArray
import io.idml.ast.{IdmlFunction, Node}
import io.idml._

import scala.collection.{immutable, mutable}

/*
 * This is functionally the same as ExtractFunction, but is kept as a separate type for usability reasons
 */
case class MapFunction(expr: Node) extends IdmlFunction {
  def args: immutable.Nil.type = Nil

  def name: String = "map"

  protected def extractOpt(ctx: IdmlContext, item: IdmlValue): Option[IdmlValue] = {
    ctx.scope = item
    ctx.cursor = item
    expr.invoke(ctx)
    if (ctx.cursor.isInstanceOf[IdmlNothing]) {
      None
    } else {
      Some(ctx.cursor)
    }
  }

  /** Applies the expression to each item in the cursor */
  override def invoke(ctx: IdmlContext): Unit = {
    // Preserve context
    val oldScope  = ctx.scope
    val oldOutput = ctx.output

    // Iterate items in the array
    ctx.cursor match {
      case nothing: IdmlNothing =>
        nothing
      case array: IdmlArray     =>
        val results: mutable.Buffer[IdmlValue] =
          array.items.flatMap(extractOpt(ctx, _))
        if (results.nonEmpty) {
          ctx.cursor = IArray(results)
        } else {
          ctx.cursor = NoFields
        }
      case _                    =>
        ctx.cursor = InvalidCaller
    }

    ctx.scope = oldScope
    ctx.output = oldOutput
  }
}
