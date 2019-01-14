package io.idml.ast

import io.idml.datanodes.{PArray, PObject}
import io.idml.{NoFields, PtolemyArray, PtolemyContext, PtolemyNothing, PtolemyObject, PtolemyValue}

import scala.collection.mutable

/** Perform wildcard operations like a.*.b */
case class Wildcard(tail: Pipeline) extends Expression {

  def invokeForObject(ctx: PtolemyContext, obj: PtolemyObject) {
    val res = mutable.Map[String, PtolemyValue]()
    obj.fields foreach {
      case (key, value) =>
        ctx.cursor = value
        tail.eval(ctx) match {
          case n: PtolemyNothing => ()
          case n: Any            => res(key) = n
        }
    }
    ctx.cursor = new PObject(res)
  }

  def invokeForArray(ctx: PtolemyContext, arr: PtolemyArray) {
    val res = mutable.Buffer[PtolemyValue]()
    arr.items foreach { value =>
      ctx.cursor = value
      tail.eval(ctx) match {
        case n: PtolemyNothing => ()
        case n: Any            => res.append(n)
      }
    }
    ctx.cursor = new PArray(res)
  }

  def invoke(ctx: PtolemyContext) {
    ctx.cursor match {
      case obj: PtolemyObject => invokeForObject(ctx, obj)
      case arr: PtolemyArray  => invokeForArray(ctx, arr)
      case _                  => ctx.cursor = NoFields
    }
  }
}
