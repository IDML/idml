package io.idml

import io.idml.datanodes.{PBool, PFalse, PString, PTrue}

/** The PtolemyValue for containing natural numbers */
trait PtolemyInt extends PtolemyValue {

  @deprecated(message = "Use toStringOption and related functions instead", since = "1.3.0")
  def formatValue: Long = value

  /** The natural number for this PtolemyValue */
  def value: Long

  override def equals(o: Any): Boolean = o match {
    case n: PtolemyDouble => n.value == value
    case n: PtolemyInt    => n.value == value
    case _                => false
  }

  override def string(): PString = PString(value.toString)

  override def bool(): PBool = if (value == 0) PFalse else PTrue

  override def hashCode(): Int = value.hashCode()

  override def toStringOption: Some[String] = Some(value.toString)
}
