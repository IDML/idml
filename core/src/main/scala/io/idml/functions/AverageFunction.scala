package io.idml.functions

import io.idml.datanodes.PDouble
import io.idml.{IdmlArray, IdmlNothing, IdmlValue, InvalidCaller}

/** Calculate an average value */
case object AverageFunction extends IdmlFunction0 {

  def name: String = "average"

  protected def apply(cursor: IdmlValue): IdmlValue = {
    cursor match {
      case nothing: IdmlNothing =>
        nothing
      case array: IdmlArray if array.items.size > 0 =>
        // FIXME: turn Idml into a numeric and replace this with sum()
        val sum = array.items.reduce((l, r) => l.+(r))
        sum / PDouble(array.items.size)
      case other: Any => InvalidCaller
    }
  }
}
