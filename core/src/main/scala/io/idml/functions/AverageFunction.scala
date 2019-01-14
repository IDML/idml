package io.idml.functions

import io.idml.datanodes.PDouble
import io.idml.{InvalidCaller, PtolemyArray, PtolemyNothing, PtolemyValue}

/** Calculate an average value */
case object AverageFunction extends PtolemyFunction0 {

  def name: String = "average"

  protected def apply(cursor: PtolemyValue): PtolemyValue = {
    cursor match {
      case nothing: PtolemyNothing =>
        nothing
      case array: PtolemyArray if array.items.size > 0 =>
        // FIXME: turn Ptolemy into a numeric and replace this with sum()
        val sum = array.items.reduce((l, r) => l.+(r))
        sum / PDouble(array.items.size)
      case other: Any => InvalidCaller
    }
  }
}
