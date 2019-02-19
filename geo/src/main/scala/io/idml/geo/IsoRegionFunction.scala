package io.idml.geo

import java.nio.charset.Charset

import io.idml.ast.{Pipeline, PtolemyFunction}
import io.idml.functions.PtolemyFunction2
import io.idml.{PtolemyJson, PtolemyValue}
import com.google.common.io.Resources

object IsoRegionFunction {
  // scalastyle:off null
  /** The ISO region name data of the form Map[country_code, Map[region_name, region_name]] */
  val Regions: PtolemyValue = PtolemyJson.parse(
    Resources
      .toString(Resources.getResource("com/datasift/ptolemy/geo/Regions.json"), Charset.defaultCharset())
      .ensuring(_ != null)
  )
  // scalastyle:on null
}

/** Transform ISO3166 country and region codes into region name */
case class IsoRegionFunction(arg1: Pipeline, arg2: Pipeline) extends PtolemyFunction2 {
  override protected def apply(cursor: PtolemyValue, country: PtolemyValue, region: PtolemyValue): PtolemyValue = {
    IsoRegionFunction.Regions.get(country).get(region)
  }
  override def name: String = "region"
}
