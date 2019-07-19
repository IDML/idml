package io.idml.functions

import io.idml.datanodes.IArray
import io.idml.{IdmlArray, IdmlContext, IdmlNothing, IdmlValue, InvalidCaller, NoFields}
import io.idml.ast.{IdmlFunction, Node}

import scala.collection.{immutable, mutable}

case class ExtractFunction(expr: Node) extends IdmlFunction {
  def args: immutable.Nil.type = Nil

  def name: String = "extract"

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
      case array: IdmlArray =>
        val results: mutable.Buffer[IdmlValue] =
          array.items.flatMap(extractOpt(ctx, _))
        if (results.nonEmpty) {
          ctx.cursor = IArray(results)
        } else {
          ctx.cursor = NoFields
        }
      case _ =>
        ctx.cursor = InvalidCaller
    }

    ctx.scope = oldScope
    ctx.output = oldOutput
  }
}
