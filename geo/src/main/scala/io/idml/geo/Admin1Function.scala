package io.idml.geo

import io.idml.datanodes.{PInt, PObject, PString}
import io.idml._
import io.idml.ast.Pipeline
import io.idml.functions.IdmlFunction1
import cats._, cats.implicits._, cats.syntax._, cats.effect._
import doobie.implicits._, doobie._
import doobie.hikari._, doobie.hikari.implicits._
import org.slf4j.LoggerFactory

class Admin1Function(driver: String, url: String, user: String, password: String) {
  val log = LoggerFactory.getLogger(getClass)

  lazy val xa = HikariTransactor
    .newHikariTransactor[IO](
      driver,
      url,
      user,
      password
    )
    .unsafeRunSync()

  case class Admin1(id: Long, name: String, asciiname: String) {
    def toPValue: IdmlValue = {
      PObject(
        "id"        -> PInt(id),
        "name"      -> PString(name),
        "asciiname" -> PString(asciiname),
      )
    }
  }

  def find(id: Long): ConnectionIO[Option[Admin1]] =
    sql"select * from Admin1 where id = $id".query[Admin1].option

  def get(id: Long): Option[IdmlValue] =
    find(id)
      .transact(xa)
      .attempt
      .unsafeRunSync()
      .leftMap { e =>
        log.warn("admin1 couldn't be called", e)
        None
      }
      .merge
      .map(_.toPValue)
  case class Admin1Function(arg: Pipeline) extends IdmlFunction1 {
    override protected def apply(cursor: IdmlValue, country: IdmlValue): IdmlValue = {
      country match {
        case i: PInt => get(i.value).getOrElse(MissingField)
        case _       => InvalidParameters
      }
    }

    override def name: String = "admin1"
  }
}
