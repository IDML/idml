package io.idml.geo

import io.idml.ast.{Argument, Pipeline, PtolemyFunction, PtolemyFunctionMetadata}
import io.idml.functions.FunctionResolver

class GeoFunctionResolver extends FunctionResolver {
  override def resolve(name: String, args: List[Argument]): Option[PtolemyFunction] = {
    (name, args) match {
      case ("geo", Nil) =>
        Some(GeoFunction)
      case ("geo", (lat: Pipeline) :: (long: Pipeline) :: Nil) =>
        Some(Geo2Function(lat, long))
      case ("country", (country: Pipeline) :: Nil) =>
        Some(IsoCountryFunction(country))
      case ("region", (country: Pipeline) :: (region: Pipeline) :: Nil) =>
        Some(IsoRegionFunction(country, region))
      case _ => None
    }
  }
  override def providedFunctions(): List[PtolemyFunctionMetadata] = List(
    PtolemyFunctionMetadata("geo", List.empty, "turn this into a geo, by using the lat/long fields"),
    PtolemyFunctionMetadata("geo", List("lat"         -> "latitude", "long" -> "longitude"), "create a geo object from a lat and a long"),
    PtolemyFunctionMetadata("country", List("country" -> "country code to look up"), "look up a country code"),
    PtolemyFunctionMetadata("region", List("region"   -> "region name to look up"), "look up a region"),
    PtolemyFunctionMetadata("city", List("city"       -> "city name to look up"), "look up a city"),
    PtolemyFunctionMetadata("admin1", List("admin1"   -> "admin1 name to look up"), "look up an admin1 area"),
  )
}

class GeoDatabaseFunctionResolver
    extends InnerGeoDatabaseFunctionResolver(
      driver = System.getenv("IDML_GEO_DB_DRIVER"),
      cityUrl = System.getenv("IDML_GEO_CITY_JDBC_URL"),
      admin1Url = System.getenv("IDML_GEO_ADMIN1_JDBC_URL"),
      user = Option(System.getenv("IDML_GEO_DB_USER")).getOrElse(""),
      password = Option(System.getenv("IDML_GEO_DB_PASSWORD")).getOrElse("")
    )

class InnerGeoDatabaseFunctionResolver(driver: String, cityUrl: String, admin1Url: String, user: String, password: String)
    extends FunctionResolver {
  val cityFunction   = Option(driver).map(_ => new CityFunction(driver, cityUrl, user, password))
  val admin1Function = Option(driver).map(_ => new Admin1Function(driver, admin1Url, user, password))

  override def providedFunctions(): List[PtolemyFunctionMetadata] =
    List(
      PtolemyFunctionMetadata("city", List("city"     -> "city name to look up"), "look up a city"),
      PtolemyFunctionMetadata("admin1", List("admin1" -> "admin1 name to look up"), "look up an admin1 area")
    )

  override def resolve(name: String, args: List[Argument]): Option[PtolemyFunction] = {
    (name, args) match {
      case ("city", (city: Pipeline) :: Nil) =>
        cityFunction.map(_.CityFunction(city))
      case ("admin1", (admin1: Pipeline) :: Nil) =>
        admin1Function.map(_.Admin1Function(admin1))
      case _ => None
    }
  }

}
