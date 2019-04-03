package io.idml.geo

import cats.effect._
import cats.implicits._
import doobie._
import doobie.hikari._
import doobie.implicits._
import io.idml._
import io.idml.ast.Pipeline
import io.idml.datanodes.{PInt, PObject, PString}
import io.idml.functions.PtolemyFunction1
import net.iakovlev.timeshape.TimeZoneEngine

class TimezoneFunction {
  val engine: TimeZoneEngine = TimeZoneEngine.initialize()

  def query(g: Geo): Option[String] = {
    engine.query(g.lat, g.long).map[Option[String]](r => Some(r.toString)).orElse(None)
  }

  case class TimezoneFunction(arg: Pipeline) extends PtolemyFunction1 {
    override protected def apply(cursor: PtolemyValue, country: PtolemyValue): PtolemyValue = {
      country match {
        case g: Geo =>  query(g).map(PString).getOrElse(MissingField)
        case _       => InvalidParameters
      }
    }

    override def name: String = "timezone"
  }
}
