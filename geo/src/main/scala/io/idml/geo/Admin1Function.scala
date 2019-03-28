package io.idml.geo

import io.idml.datanodes.{PInt, PObject, PString}
import io.idml._
import io.idml.ast.Pipeline
import io.idml.functions.PtolemyFunction1
import cats._, cats.implicits._, cats.syntax._, cats.effect._
import doobie.implicits._, doobie._
import doobie.hikari._, doobie.hikari.implicits._

class Admin1Function(driver: String, url: String, user: String, password: String) {
  lazy val xa = HikariTransactor
    .newHikariTransactor[IO](
      driver,
      url,
      user,
      password
    )
    .unsafeRunSync()

  case class Admin1(id: Long, name: String, asciiname: String) {
    def toPValue: PtolemyValue = {
      PObject(
        "id"        -> PInt(id),
        "name"      -> PString(name),
        "asciiname" -> PString(asciiname),
      )
    }
  }

  def find(id: Long): ConnectionIO[Option[Admin1]] =
    sql"select * from Admin1 where id = $id".query[Admin1].option

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
  case class Admin1Function(arg: Pipeline) extends PtolemyFunction1 {
    override protected def apply(cursor: PtolemyValue, country: PtolemyValue): PtolemyValue = {
      country match {
        case i: PInt => get(i.value).getOrElse(MissingField)
        case _       => InvalidParameters
      }
    }

    override def name: String = "admin1"
  }
}
