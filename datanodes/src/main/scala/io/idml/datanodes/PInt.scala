package io.idml.datanodes

import io.idml.{PtolemyInt, PtolemyValue}

/** The default PtolemyValue implementation of an integer */
case class PInt(value: Long) extends PtolemyInt {

  /** Transform this value into a natural number */
  override def int(): PInt = this

  /** Transform this value into a floating point */
  override def float(): PtolemyValue = PtolemyValue(value.toDouble)

  // FIXME: PInt is currently implemented as PLong!
  override def toIntOption: Some[Int] = Some(value.toInt)

  override def toLongOption: Some[Long] = Some(value)

  override def toDoubleOption = Some(value.toDouble)
}
