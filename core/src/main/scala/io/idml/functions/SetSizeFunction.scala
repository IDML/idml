package io.idml.functions

import io.idml.datanodes.PString
import io.idml.{InvalidCaller, InvalidParameters, PtolemyArray, PtolemyContext, PtolemyNothing, PtolemyString}
import io.idml.ast.{Pipeline, PtolemyFunction}

import scala.collection.immutable

/** Set the size of something */
case class SetSizeFunction(arg: Pipeline) extends PtolemyFunction {
  override def name: String = "size"

  override def args: immutable.Nil.type = Nil

  override def invoke(ctx: PtolemyContext): Unit = {
    arg.eval(ctx).toIntOption.filter(_ >= 0) match {
      case Some(maxSize) =>
        ctx.cursor = ctx.cursor match {
          case nothing: PtolemyNothing =>
            nothing
          case array: PtolemyArray =>
            array.slice(None, Some(maxSize))
          case string: PtolemyString if string.value == "" || string.value.length <= maxSize =>
            string
          case string: PtolemyString =>
            PString(string.value.substring(0, maxSize))
          case other: Any =>
            InvalidCaller
        }
      case None =>
        ctx.cursor = InvalidParameters
    }
  }
}
