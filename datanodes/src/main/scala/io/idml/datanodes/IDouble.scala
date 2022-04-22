package io.idml.datanodes

import io.idml.{IdmlDouble, IdmlValue}

/** The default IdmlValue implementation of a floating point number */
case class IDouble(value: Double) extends IdmlDouble {

  /** Transform this value into a floating point number */
  override def float(): IDouble = this

  /** Transform this value into a natural number */
  override def int(): IdmlValue = IdmlValue(value.toInt)

  override def toDoubleOption = Some(value)
}
