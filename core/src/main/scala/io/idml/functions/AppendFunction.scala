package io.idml.functions

import io.idml.datanodes.IArray
import io.idml.{IdmlArray, IdmlNothing, IdmlValue, InvalidCaller}
import io.idml.ast.Pipeline

/** Append an argument to the calling array if it evaluated successfully */
case class AppendFunction(arg: Pipeline) extends IdmlFunction1 {
  override def name: String = "append"

  override def apply(cursor: IdmlValue, input: IdmlValue): IdmlValue = {
    cursor match {
      case nothing: IdmlNothing =>
        nothing
      case arr: IdmlArray =>
        input match {
          case nothing: IdmlNothing =>
            // Do nothing to the cursor
            arr
          case _ =>
            arr.deepCopy
            // Create a new array with the original values and a new one on the end
            IArray(arr.items.map(_.deepCopy) :+ input)
        }
      case _ =>
        InvalidCaller
    }
  }
}
