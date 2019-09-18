package io.circe.idml.utils

import io.circe._
import io.idml.datanodes.{IBigDecimal, IBigInt, IDouble, IInt}

object JsonNumberFolder {

  def fold(jn: JsonNumber) = jn match {
    case number: BiggerDecimalJsonNumber =>
      number.toLong
        .map(IInt)
        .orElse(
          number.toBigInt.map(IBigInt.apply)
        )
        .orElse(
          number.toBigDecimal.map(IBigDecimal.apply)
        )
        .getOrElse(
          IDouble(number.toDouble)
        )
    case JsonBigDecimal(value) =>
      IBigDecimal(value)
    case JsonLong(value) =>
      IInt(value)
    case JsonDouble(value) =>
      IDouble(value)
    case JsonFloat(value) =>
      IDouble(value.toDouble)

  }

}
