package io.idml.functions

import io.idml.datanodes.IArray
import io.idml._
import io.idml.ast._

import scala.collection.{immutable, mutable}

case class FilterFunction(expr: Node) extends IdmlFunction {
  def args: immutable.Nil.type = Nil

  def name: String = "filter"

  protected def filterOpt(ctx: IdmlContext, item: IdmlValue): Option[IdmlValue] = {
    ctx.scope = item
    ctx.cursor = item
    expr.invoke(ctx)
    if (ctx.cursor.isInstanceOf[IdmlNothing] || ctx.cursor == Filtered || ctx.cursor.toBoolOption.contains(false)) {
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
          array.items.flatMap(x => filterOpt(ctx, x))
        if (results.nonEmpty) {
          ctx.cursor = IArray(results)
        } else {
          ctx.cursor = NoFields
        }
      case v: IdmlValue =>
        ctx.cursor = filterOpt(ctx, v).getOrElse(Filtered)
      case _ =>
        ctx.cursor = InvalidCaller
    }

    ctx.scope = oldScope
    ctx.output = oldOutput
  }
}
