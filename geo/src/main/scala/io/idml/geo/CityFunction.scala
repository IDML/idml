package io.idml.geo

import io.idml.datanodes.{PInt, PObject, PString}
import io.idml._
import io.idml.ast.Pipeline
import io.idml.functions.PtolemyFunction1
import cats._, cats.implicits._, cats.syntax._, cats.effect._
import doobie.implicits._, doobie._
import doobie.hikari._, doobie.hikari.implicits._

class CityFunction(driver: String, url: String, user: String, password: String) {
  lazy val xa = HikariTransactor
    .newHikariTransactor[IO](
      driver,
      url,
      user,
      password
    )
    .unsafeRunSync()

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
    sql"select * from cities where id = $id".query[City].option

  def get(id: Long): Option[PtolemyValue] =
    find(id)
      .transact(xa)
      .attempt
      .unsafeRunSync()
      .leftMap { _ =>
        // could log the error here
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
