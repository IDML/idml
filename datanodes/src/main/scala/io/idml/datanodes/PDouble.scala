package io.idml.datanodes

import io.idml.{PtolemyDouble, PtolemyValue}

/** The default PtolemyValue implementation of a floating point number*/
case class PDouble(value: Double) extends PtolemyDouble {

  /** Transform this value into a floating point number */
  override def float(): PDouble = this

  /** Transform this value into a natural number */
  override def int(): PtolemyValue = PtolemyValue(value.toInt)

  override def toDoubleOption = Some(value)
}
