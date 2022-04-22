package io.idml.geo

import java.nio.charset.Charset

import com.google.common.io.Resources
import io.idml.{IdmlJson, IdmlValue}
import io.idml.ast.{Argument, IdmlFunction, IdmlFunctionMetadata, Pipeline}
import io.idml.functions.FunctionResolver

class DefaultGeoFunctionResolver extends GeoFunctionResolver(IdmlJson.load())

class GeoFunctionResolver(json: IdmlJson) extends FunctionResolver {

  lazy val countries: IdmlValue = json.parse(
    Resources
      .toString(Resources.getResource(getClass, "Countries.json"), Charset.defaultCharset())
      .ensuring(_ != null)
  )

  lazy val regions: IdmlValue = json.parse(
    Resources
      .toString(Resources.getResource(getClass, "Regions.json"), Charset.defaultCharset())
      .ensuring(_ != null)
  )

  override def resolve(name: String, args: List[Argument]): Option[IdmlFunction] = {
    (name, args) match {
      case ("geo", Nil)                                                 =>
        Some(GeoFunction)
      case ("geo", (lat: Pipeline) :: (long: Pipeline) :: Nil)          =>
        Some(Geo2Function(lat, long))
      case ("country", (country: Pipeline) :: Nil)                      =>
        Some(new IsoCountryFunction(countries, country))
      case ("region", (country: Pipeline) :: (region: Pipeline) :: Nil) =>
        Some(new IsoRegionFunction(regions, country, region))
      case ("timezone", Nil)                                            =>
        Some(TimezoneFunction.TimezoneFunction)
      case _                                                            => None
    }
  }
  override def providedFunctions(): List[IdmlFunctionMetadata]                   =
    List(
      IdmlFunctionMetadata("geo", List.empty, "turn this into a geo, by using the lat/long fields"),
      IdmlFunctionMetadata(
        "geo",
        List("lat" -> "latitude", "long" -> "longitude"),
        "create a geo object from a lat and a long"),
      IdmlFunctionMetadata(
        "country",
        List("country" -> "country code to look up"),
        "look up a country code"),
      IdmlFunctionMetadata(
        "region",
        List("region" -> "region name to look up"),
        "look up a region"),
      IdmlFunctionMetadata(
        "timezone",
        List.empty,
        "turn this geo into a timezone, eg. Europe/London")
    )

}
