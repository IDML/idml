package io.idml.geo

import java.nio.charset.Charset

import io.idml.{PtolemyJson, PtolemyValue}
import io.idml.ast.Pipeline
import io.idml.functions.PtolemyFunction1
import com.google.common.io.Resources

object IsoCountryFunction {
  // scalastyle:off null
  /** The ISO country name data of the form Map[country_code] = country_name */
  val Countries: PtolemyValue = PtolemyJson.parse(
    Resources
      .toString(Resources.getResource("com/datasift/ptolemy/geo/Countries.json"), Charset.defaultCharset())
      .ensuring(_ != null)
  )
  // scalastyle:on null
}

/** Turns iso countries into country names */
case class IsoCountryFunction(arg: Pipeline) extends PtolemyFunction1 {
  override protected def apply(cursor: PtolemyValue, country: PtolemyValue): PtolemyValue = {
    IsoCountryFunction.Countries.get(country)
  }

  override def name: String = "country"
}
