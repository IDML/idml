package io.idml.geo

import io.idml.{CastFailed, CastUnsupported, PtolemyArray, PtolemyObject, PtolemyValue}
import io.idml.functions.PtolemyFunction0

/**
  * Constructor for geolocation objects
  */
case object GeoFunction extends PtolemyFunction0 {
  override protected def apply(cursor: PtolemyValue): PtolemyValue = {
    cursor match {
      case obj: PtolemyObject =>
        Geo(obj.get("latitude").float(), obj.get("longitude").float())
      case arr: PtolemyArray if arr.size > 1 =>
        val lat  = arr.items.head.float()
        val long = arr.items(1).float()
        if (lat.isNothing || lat.isNothing) {
          CastFailed
        } else {
          Geo(lat, long)
        }
      case arr: PtolemyArray => CastFailed
      case _                 => CastUnsupported
    }
  }

  override def name: String = "geo"
}
