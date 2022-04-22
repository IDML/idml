package io.idml

import io.idml.datanodes.{IBool, IFalse, ITrue}

/** The IdmlValue that contains floating point numbers */
trait IdmlDouble extends IdmlValue {

  def formatValue: Double = value

  /** The floating point number for this IdmlValue */
  def value: Double

  override def equals(o: Any): Boolean =
    o match {
      case n: IdmlDouble => n.value == value
      case n: IdmlInt    => n.value == value
      case _             => false
    }
  override def hashCode(): Int         = value.hashCode()

  override def bool(): IBool = if (value == 0) IFalse else ITrue
}
