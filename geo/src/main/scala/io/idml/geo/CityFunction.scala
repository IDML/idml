package io.idml.geo

import io.idml.datanodes.{PInt, PObject, PString}
import io.idml._
import io.idml.ast.Pipeline
import io.idml.functions.PtolemyFunction1
import cats._, cats.implicits._, cats.syntax._, cats.effect._
import doobie.implicits._, doobie._
import doobie.hikari._, doobie.hikari.implicits._
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.global

class CityFunction(driver: String, url: String, user: String, password: String) {
  val log         = LoggerFactory.getLogger(getClass)
  implicit val cs = IO.contextShift(global)

  lazy val xa = HikariTransactor
    .newHikariTransactor[IO](
      driver,
      url,
      user,
      password,
      global,
      global
    )
    .allocated
    .unsafeRunSync()
    ._1

  case class City(id: Long, name: String, asciiname: String) {
    def toPValue: PtolemyValue = {
      PObject(
        "id"        -> PInt(id),
        "name"      -> PString(name),
        "asciiname" -> PString(asciiname)
      )
    }
  }

  def find(id: Long): ConnectionIO[Option[City]] =
    sql"select * from Cities where id = $id".query[City].option

  def get(id: Long): Option[PtolemyValue] =
    find(id)
      .transact(xa)
      .attempt
      .unsafeRunSync()
      .leftMap { e =>
        log.warn("city couldn't be called", e)
        None
      }
      .merge
      .map(_.toPValue)

  case class CityFunction(arg: Pipeline) extends PtolemyFunction1 {
    override protected def apply(cursor: PtolemyValue, country: PtolemyValue): PtolemyValue = {
      country match {
        case i: PInt => get(i.value).getOrElse(MissingField)
        case _       => InvalidParameters
      }
    }

    override def name: String = "city"
  }
}
