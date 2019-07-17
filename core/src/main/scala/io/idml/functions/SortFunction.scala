package io.idml.functions

import io.idml.datanodes.PArray
import io.idml.ast.{IdmlFunction, Node}
import io.idml._

import scala.collection.{immutable, mutable}

case class SortFunction(expr: Node) extends IdmlFunction {
  def args: immutable.Nil.type = Nil

  def name: String = "sort"

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
          array.items.flatMap(x => extractOpt(ctx, x).map(x -> _)).sortBy(_._2).map(_._1)
        if (results.nonEmpty) {
          ctx.cursor = PArray(results)
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
