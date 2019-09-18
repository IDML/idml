package io.idml

import io.idml.datanodes.{IBool, IFalse, IString, ITrue}

/** The IdmlValue for containing very large decimals */
trait IdmlBigDecimal extends IdmlValue {

  @deprecated(message = "Use toStringOption and related functions instead", since = "1.3.0")
  def formatValue: BigDecimal = value

  /** The natural number for this IdmlValue */
  def value: BigDecimal

  override def equals(o: Any): Boolean = o match {
    case b: IdmlBigDecimal => b.value == value
    case n: IdmlBigInt     => value == n.value
    case n: IdmlDouble     => n.value == value
    case n: IdmlInt        => n.value == value
    case _                 => false
  }

  override def string(): IString = IString(value.toString)

  override def bool(): IBool = if (value == 0) IFalse else ITrue

  override def hashCode(): Int = value.hashCode()

  override def toStringOption: Some[String] = Some(value.toString)
}
