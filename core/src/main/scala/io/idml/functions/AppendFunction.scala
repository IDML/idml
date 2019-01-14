package io.idml.functions

import io.idml.datanodes.PArray
import io.idml.{InvalidCaller, PtolemyArray, PtolemyNothing, PtolemyValue}
import io.idml.ast.Pipeline

/** Append an argument to the calling array if it evaluated successfully */
case class AppendFunction(arg: Pipeline) extends PtolemyFunction1 {
  override def name: String = "append"

  override def apply(cursor: PtolemyValue, input: PtolemyValue): PtolemyValue = {
    cursor match {
      case nothing: PtolemyNothing =>
        nothing
      case arr: PtolemyArray =>
        input match {
          case nothing: PtolemyNothing =>
            // Do nothing to the cursor
            arr
          case _ =>
            arr.deepCopy
            // Create a new array with the original values and a new one on the end
            PArray(arr.items.map(_.deepCopy) :+ input)
        }
      case _ =>
        InvalidCaller
    }
  }
}
