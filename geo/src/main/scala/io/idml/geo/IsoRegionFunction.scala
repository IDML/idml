package io.idml.geo

import java.nio.charset.Charset

import io.idml.ast.{Pipeline, PtolemyFunction}
import io.idml.functions.PtolemyFunction2
import io.idml.{PtolemyJson, PtolemyValue}
import com.google.common.io.Resources

/** Transform ISO3166 country and region codes into region name */
class IsoRegionFunction(regions: => PtolemyValue, val arg1: Pipeline, val arg2: Pipeline) extends PtolemyFunction2 {
  override protected def apply(cursor: PtolemyValue, country: PtolemyValue, region: PtolemyValue): PtolemyValue = {
    regions.get(country).get(region)
  }
  override def name: String = "region"
}
