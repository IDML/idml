package io.idml.geo

import cats.implicits._
import io.idml._
import io.idml.ast.Pipeline
import io.idml.datanodes.{IInt, IObject, IString}
import io.idml.functions.IdmlFunction0
import net.iakovlev.timeshape.TimeZoneEngine

object TimezoneFunction {
  lazy val engine: TimeZoneEngine = TimeZoneEngine.initialize()

  def query(g: Geo): Option[String] = {
    engine.query(g.lat, g.long).map[Option[String]](r => Some(r.toString)).orElse(None)
  }

  case object TimezoneFunction extends IdmlFunction0 {
    override protected def apply(cursor: IdmlValue): IdmlValue = {
      cursor match {
        case g: Geo => query(g).map(IString).getOrElse(MissingField)
        case _      => InvalidParameters
      }
    }

    override def name: String = "timezone"
  }
}
