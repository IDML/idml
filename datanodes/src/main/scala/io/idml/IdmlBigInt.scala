package io.idml

import io.idml.datanodes._

/** The IdmlValue for containing natural numbers */
trait IdmlBigInt extends IdmlValue {

  @deprecated(message = "Use toStringOption and related functions instead", since = "1.3.0")
  def formatValue: BigInt = value

  /** The natural number for this IdmlValue */
  def value: BigInt

  def toBigDecimal: IdmlBigDecimal = IBigDecimal(BigDecimal(value))

  override def equals(o: Any): Boolean = o match {
    case n: IdmlBigInt     => n.value == value
    case n: IdmlBigDecimal => n.value == value
    case n: IdmlDouble     => n.value == value
    case n: IdmlInt        => n.value == value
    case _                 => false
  }

  override def string(): IString = IString(value.toString)

  override def bool(): IBool = if (value == 0) IFalse else ITrue

  override def hashCode(): Int = value.hashCode()

  override def toStringOption: Some[String] = Some(value.toString)
}
