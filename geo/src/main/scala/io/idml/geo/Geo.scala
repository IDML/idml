package io.idml.geo

import io.idml.datanodes.CompositeValue
import io.idml.{CastFailed, IdmlDouble, IdmlObject, IdmlValue}

import scala.collection.mutable

/** A geo-tag object */
case class Geo(lat: Double, long: Double) extends IdmlObject with CompositeValue {

  /** The fields */
  val fields = mutable.Map(
    "latitude"  -> IdmlValue(lat),
    "longitude" -> IdmlValue(long)
  )
}

/** Helpers for geo-tagging */
object Geo {

  /** Transform arbitrary nodes into geo-tags */
  def apply(lat: IdmlValue, long: IdmlValue): IdmlValue =
    (lat, long) match {
      case (lat: IdmlDouble, long: IdmlDouble) =>
        Geo(lat.value, long.value)
      case _                                   => CastFailed
    }
}
