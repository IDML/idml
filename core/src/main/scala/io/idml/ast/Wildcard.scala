package io.idml.ast

import io.idml.datanodes.{IArray, IObject}
import io.idml.{IdmlArray, IdmlContext, IdmlNothing, IdmlObject, IdmlValue, NoFields}

import scala.collection.mutable

/** Perform wildcard operations like a.*.b */
case class Wildcard(tail: Pipeline) extends Expression {

  def invokeForObject(ctx: IdmlContext, obj: IdmlObject) {
    val res = mutable.SortedMap[String, IdmlValue]()
    obj.fields foreach {
      case (key, value) =>
        ctx.cursor = value
        tail.eval(ctx) match {
          case n: IdmlNothing => ()
          case n: Any         => res(key) = n
        }
    }
    ctx.cursor = new IObject(res)
  }

  def invokeForArray(ctx: IdmlContext, arr: IdmlArray) {
    val res = mutable.Buffer[IdmlValue]()
    arr.items foreach { value =>
      ctx.cursor = value
      tail.eval(ctx) match {
        case n: IdmlNothing => ()
        case n: Any         => res.append(n)
      }
    }
    ctx.cursor = new IArray(res)
  }

  def invoke(ctx: IdmlContext) {
    ctx.cursor match {
      case obj: IdmlObject => invokeForObject(ctx, obj)
      case arr: IdmlArray  => invokeForArray(ctx, arr)
      case _               => ctx.cursor = NoFields
    }
  }
}
