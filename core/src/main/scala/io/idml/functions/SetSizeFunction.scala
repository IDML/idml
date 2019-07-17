package io.idml.functions

import io.idml.datanodes.PString
import io.idml.{IdmlArray, IdmlContext, IdmlNothing, IdmlString, InvalidCaller, InvalidParameters}
import io.idml.ast.{IdmlFunction, Pipeline}

import scala.collection.immutable

/** Set the size of something */
case class SetSizeFunction(arg: Pipeline) extends IdmlFunction {
  override def name: String = "size"

  override def args: immutable.Nil.type = Nil

  override def invoke(ctx: IdmlContext): Unit = {
    arg.eval(ctx).toIntOption.filter(_ >= 0) match {
      case Some(maxSize) =>
        ctx.cursor = ctx.cursor match {
          case nothing: IdmlNothing =>
            nothing
          case array: IdmlArray =>
            array.slice(None, Some(maxSize))
          case string: IdmlString if string.value == "" || string.value.length <= maxSize =>
            string
          case string: IdmlString =>
            PString(string.value.substring(0, maxSize))
          case other: Any =>
            InvalidCaller
        }
      case None =>
        ctx.cursor = InvalidParameters
    }
  }
}
