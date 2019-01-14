package io.idml.functions

import io.idml.datanodes.PInt
import io.idml.{InvalidCaller, PtolemyArray, PtolemyContext, PtolemyNothing, PtolemyString}
import io.idml.ast.PtolemyFunction

import scala.collection.immutable

/** Get the size of something */
object GetSizeFunction extends PtolemyFunction {
  override def name: String             = "size"
  override def args: immutable.Nil.type = Nil

  override def invoke(ctx: PtolemyContext): Unit = {
    ctx.cursor = ctx.cursor match {
      case nothing: PtolemyNothing =>
        nothing
      case array: PtolemyArray =>
        PInt(array.items.size)
      case string: PtolemyString =>
        PInt(string.value.length)
      case _ =>
        InvalidCaller
    }
  }
}
