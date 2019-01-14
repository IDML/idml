package io.idml.functions

import io.idml.datanodes.PArray
import io.idml._
import io.idml.ast._

import scala.collection.{immutable, mutable}

case class FilterFunction(expr: Node) extends PtolemyFunction {
  def args: immutable.Nil.type = Nil

  def name: String = "filter"

  protected def filterOpt(ctx: PtolemyContext, item: PtolemyValue): Option[PtolemyValue] = {
    ctx.scope = item
    ctx.cursor = item
    expr.invoke(ctx)
    if (ctx.cursor.isInstanceOf[PtolemyNothing] || ctx.cursor == Filtered || ctx.cursor.toBoolOption.contains(false)) {
      None
    } else {
      Some(ctx.cursor)
    }
  }

  override def invoke(ctx: PtolemyContext): Unit = {
    // Preserve context
    val oldScope  = ctx.scope
    val oldOutput = ctx.output

    // Iterate items in the array
    ctx.cursor match {
      case nothing: PtolemyNothing =>
        nothing
      case array: PtolemyArray =>
        val results: mutable.Buffer[PtolemyValue] =
          array.items.flatMap(x => filterOpt(ctx, x))
        if (results.nonEmpty) {
          ctx.cursor = PArray(results)
        } else {
          ctx.cursor = NoFields
        }
      case v: PtolemyValue =>
        ctx.cursor = filterOpt(ctx, v).getOrElse(Filtered)
      case _ =>
        ctx.cursor = InvalidCaller
    }

    ctx.scope = oldScope
    ctx.output = oldOutput
  }
}
