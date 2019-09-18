package io.idml.datanodes

import java.math.BigInteger

import io.idml.{IdmlBigInt, IdmlValue}

/** The default IdmlValue implementation of a bignteger */
case class IBigInt(value: BigInt) extends IdmlBigInt {

  /** Transform this value into a natural number */
  override def int(): IInt = IInt(value.toInt)

  /** Transform this value into a floating point */
  override def float(): IdmlValue = IdmlValue(value.toDouble)

  // FIXME: PInt is currently implemented as PLong!
  override def toIntOption: Some[Int] = Some(value.toInt)

  override def toLongOption: Some[Long] = Some(value.toLong)

  override def toDoubleOption = Some(value.toDouble)
}

object IBigInt {

  /** Create one from a java BigInteger */
  def of(jbi: BigInteger): IBigInt = IBigInt(jbi)
}
