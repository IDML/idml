package io.idml.functions

import io.idml.datanodes.IInt
import io.idml.{IdmlArray, IdmlContext, IdmlNothing, IdmlString, InvalidCaller}
import io.idml.ast.IdmlFunction

import scala.collection.immutable

/** Get the size of something */
object GetSizeFunction extends IdmlFunction {
  override def name: String             = "size"
  override def args: immutable.Nil.type = Nil

  override def invoke(ctx: IdmlContext): Unit = {
    ctx.cursor = ctx.cursor match {
      case nothing: IdmlNothing =>
        nothing
      case array: IdmlArray     =>
        IInt(array.items.size)
      case string: IdmlString   =>
        IInt(string.value.length)
      case _                    =>
        InvalidCaller
    }
  }
}
