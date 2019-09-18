package io.idml.datanodes

import io.idml.{IdmlBigDecimal, IdmlValue}

/** The default IdmlValue implementation of a bigdecimal */
case class IBigDecimal(value: BigDecimal) extends IdmlBigDecimal {

  /** Transform this value into a natural number */
  override def int(): IInt = IInt(value.toInt)

  /** Transform this value into a floating point */
  override def float(): IdmlValue = IdmlValue(value.toDouble)

  // FIXME: PInt is currently implemented as PLong!
  override def toIntOption: Some[Int] = Some(value.toInt)

  override def toLongOption: Some[Long] = Some(value.toLong)

  override def toDoubleOption = Some(value.toDouble)
}

object IBigDecimal {
  def of(jbd: java.math.BigDecimal): IBigDecimal = IBigDecimal(jbd)
}
