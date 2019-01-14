package io.idml.functions

import io.idml.datanodes.{PArray, PDouble, PString}
import io.idml.ast.{Pipeline, PtolemyFunction}
import io.idml._

import scala.collection.immutable

case class ConcatFunction(sep: String) extends PtolemyFunction {

  def name: String = "concat"

  def args: immutable.Nil.type = Nil

  override def invoke(ctx: PtolemyContext): Unit = {
    ctx.cursor = ctx.cursor match {
      case PArray(items) =>
        items
          .foldLeft(Option.empty[String]) {
            case (None, i: PtolemyString)    => Some(i.value)
            case (Some(a), i: PtolemyString) => Some(a + sep + i.value)
            case (None, n: PtolemyNothing)   => None
            case (a @ Some(_), _)            => a
            case (None, v: PtolemyValue)     => v.toStringOption
          }
          .map(PString.apply)
          .getOrElse(MissingField)
      case _ => InvalidCaller
    }
  }
}
