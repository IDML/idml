package io.idml.functions

import io.idml.datanodes.PArray
import io.idml.ast.{Node, PtolemyFunction}
import io.idml._

import scala.collection.{immutable, mutable}

case class UniqueFunction(expr: Node) extends PtolemyFunction {
  def args: immutable.Nil.type = Nil

  def name: String = "unique"

  protected def extractOpt(ctx: PtolemyContext, item: PtolemyValue): Option[PtolemyValue] = {
    ctx.scope = item
    ctx.cursor = item
    expr.invoke(ctx)
    if (ctx.cursor.isInstanceOf[PtolemyNothing]) {
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
          array.items
            .flatMap(x => extractOpt(ctx, x).map(x -> _))
            .foldLeft((immutable.HashSet[PtolemyValue](), List.empty[PtolemyValue])) {
              case ((s, a), (i, k)) =>
                if (s.contains(k)) {
                  (s, a)
                } else {
                  ((s + k), (i :: a))
                }
            }
            ._2
            .reverse
            .toBuffer
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
