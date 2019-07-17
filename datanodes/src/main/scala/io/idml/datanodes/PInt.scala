package io.idml.datanodes

import io.idml.{IdmlInt, IdmlValue}

/** The default IdmlValue implementation of an integer */
case class PInt(value: Long) extends IdmlInt {

  /** Transform this value into a natural number */
  override def int(): PInt = this

  /** Transform this value into a floating point */
  override def float(): IdmlValue = IdmlValue(value.toDouble)

  // FIXME: PInt is currently implemented as PLong!
  override def toIntOption: Some[Int] = Some(value.toInt)

  override def toLongOption: Some[Long] = Some(value)

  override def toDoubleOption = Some(value.toDouble)
}
