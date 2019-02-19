package io.idml.geo

import io.idml.PtolemyValue
import io.idml.ast.Pipeline
import io.idml.functions.PtolemyFunction2

/**
  * 2-argument constructor for geolocation
  */
case class Geo2Function(arg1: Pipeline, arg2: Pipeline) extends PtolemyFunction2 {
  override protected def apply(cursor: PtolemyValue, lat: PtolemyValue, long: PtolemyValue): PtolemyValue = {
    Geo(lat.float(), long.float())
  }

  override def name: String = "geo"
}
