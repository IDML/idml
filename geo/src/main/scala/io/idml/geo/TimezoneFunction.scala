package io.idml.geo

import cats.effect._
import cats.implicits._
import doobie._
import doobie.hikari._
import doobie.implicits._
import io.idml._
import io.idml.ast.Pipeline
import io.idml.datanodes.{PInt, PObject, PString}
import io.idml.functions.PtolemyFunction0
import net.iakovlev.timeshape.TimeZoneEngine

object TimezoneFunction {
  lazy val engine: TimeZoneEngine = TimeZoneEngine.initialize()

  def query(g: Geo): Option[String] = {
    engine.query(g.lat, g.long).map[Option[String]](r => Some(r.toString)).orElse(None)
  }

  case object TimezoneFunction extends PtolemyFunction0 {
    override protected def apply(cursor: PtolemyValue): PtolemyValue = {
      cursor match {
        case g: Geo => query(g).map(PString).getOrElse(MissingField)
        case _      => InvalidParameters
      }
    }

    override def name: String = "timezone"
  }
}
