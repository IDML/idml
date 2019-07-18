package io.idml.functions

import io.idml.datanodes.IArray
import io.idml.{IdmlArray, IdmlNothing, IdmlValue, InvalidCaller}
import io.idml.ast.Pipeline

/** Prepend an argument to the calling array if it evaluated successfully */
case class PrependFunction(arg: Pipeline) extends IdmlFunction1 {
  override def name: String = "prepend"

  protected def apply(cursor: IdmlValue, val1: IdmlValue): IdmlValue = {
    cursor match {
      case nothing: IdmlNothing =>
        nothing
      case arr: IdmlArray =>
        val1 match {
          case nothing: IdmlNothing =>
            // Do nothing to the cursor
            arr
          case prependee: Any =>
            arr.deepCopy
            // Create a new array with the original values and a new one on the end
            IArray(prependee +: arr.items.map(_.deepCopy))
        }
      case other: Any =>
        InvalidCaller
    }
  }

}
