package io.idml.functions

import io.idml.datanodes.PArray
import io.idml.{InvalidCaller, PtolemyArray, PtolemyNothing, PtolemyValue}
import io.idml.ast.Pipeline

/** Prepend an argument to the calling array if it evaluated successfully */
case class PrependFunction(arg: Pipeline) extends PtolemyFunction1 {
  override def name: String = "prepend"

  protected def apply(cursor: PtolemyValue, val1: PtolemyValue): PtolemyValue = {
    cursor match {
      case nothing: PtolemyNothing =>
        nothing
      case arr: PtolemyArray =>
        val1 match {
          case nothing: PtolemyNothing =>
            // Do nothing to the cursor
            arr
          case prependee: Any =>
            arr.deepCopy
            // Create a new array with the original values and a new one on the end
            PArray(prependee +: arr.items.map(_.deepCopy))
        }
      case other: Any =>
        InvalidCaller
    }
  }

}
