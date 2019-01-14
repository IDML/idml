package io.idml

import io.idml.datanodes.{PBool, PFalse, PTrue}

/** The PtolemyValue that contains floating point numbers */
trait PtolemyDouble extends PtolemyValue {

  def formatValue: Double = value

  /** The floating point number for this PtolemyValue */
  def value: Double

  override def equals(o: Any): Boolean = o match {
    case n: PtolemyDouble => n.value == value
    case n: PtolemyInt    => n.value == value
    case _                => false
  }
  override def hashCode(): Int = value.hashCode()

  override def bool(): PBool = if (value == 0) PFalse else PTrue
}
