package io.idml.geo

import io.idml.IdmlValue
import io.idml.ast.Pipeline
import io.idml.functions.IdmlFunction2

/** 2-argument constructor for geolocation
  */
case class Geo2Function(arg1: Pipeline, arg2: Pipeline) extends IdmlFunction2 {
  override protected def apply(cursor: IdmlValue, lat: IdmlValue, long: IdmlValue): IdmlValue = {
    Geo(lat.float(), long.float())
  }

  override def name: String = "geo"
}
