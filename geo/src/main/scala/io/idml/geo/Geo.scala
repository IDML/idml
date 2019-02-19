package io.idml.geo

import io.idml.datanodes.CompositeValue
import io.idml.{CastFailed, PtolemyDouble, PtolemyObject, PtolemyValue}

import scala.collection.mutable

/** A geo-tag object */
case class Geo(lat: Double, long: Double) extends PtolemyObject with CompositeValue {

  /** The fields */
  val fields = mutable.Map(
    "latitude"  -> PtolemyValue(lat),
    "longitude" -> PtolemyValue(long)
  )
}

/** Helpers for geo-tagging */
object Geo {

  /** Transform arbitrary nodes into geo-tags */
  def apply(lat: PtolemyValue, long: PtolemyValue): PtolemyValue =
    (lat, long) match {
      case (lat: PtolemyDouble, long: PtolemyDouble) =>
        Geo(lat.value, long.value)
      case _ => CastFailed
    }
}
