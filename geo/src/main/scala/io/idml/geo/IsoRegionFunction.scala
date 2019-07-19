package io.idml.geo

import java.nio.charset.Charset

import io.idml.ast.{IdmlFunction, Pipeline}
import io.idml.functions.IdmlFunction2
import io.idml.{IdmlJson, IdmlValue}
import com.google.common.io.Resources

/** Transform ISO3166 country and region codes into region name */
class IsoRegionFunction(regions: => IdmlValue, val arg1: Pipeline, val arg2: Pipeline) extends IdmlFunction2 {
  override protected def apply(cursor: IdmlValue, country: IdmlValue, region: IdmlValue): IdmlValue = {
    regions.get(country).get(region)
  }
  override def name: String = "region"
}
