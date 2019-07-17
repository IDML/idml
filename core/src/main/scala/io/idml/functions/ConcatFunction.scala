package io.idml.functions

import io.idml.datanodes.{PArray, PDouble, PString}
import io.idml.ast.{IdmlFunction, Pipeline}
import io.idml._

import scala.collection.immutable

case class ConcatFunction(sep: String) extends IdmlFunction {

  def name: String = "concat"

  def args: immutable.Nil.type = Nil

  override def invoke(ctx: IdmlContext): Unit = {
    ctx.cursor = ctx.cursor match {
      case PArray(items) =>
        items
          .foldLeft(Option.empty[String]) {
            case (None, i: IdmlString)    => Some(i.value)
            case (Some(a), i: IdmlString) => Some(a + sep + i.value)
            case (None, n: IdmlNothing)   => None
            case (a @ Some(_), _)         => a
            case (None, v: IdmlValue)     => v.toStringOption
          }
          .map(PString.apply)
          .getOrElse(MissingField)
      case _ => InvalidCaller
    }
  }
}
