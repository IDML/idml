package io.idml.geo

import io.idml.{CastFailed, CastUnsupported, IdmlArray, IdmlObject, IdmlValue}
import io.idml.functions.IdmlFunction0

/**
  * Constructor for geolocation objects
  */
case object GeoFunction extends IdmlFunction0 {
  override protected def apply(cursor: IdmlValue): IdmlValue = {
    cursor match {
      case obj: IdmlObject =>
        Geo(obj.get("latitude").float(), obj.get("longitude").float())
      case arr: IdmlArray if arr.size > 1 =>
        val lat  = arr.items.head.float()
        val long = arr.items(1).float()
        if (lat.isNothing || lat.isNothing) {
          CastFailed
        } else {
          Geo(lat, long)
        }
      case arr: IdmlArray => CastFailed
      case _              => CastUnsupported
    }
  }

  override def name: String = "geo"
}
