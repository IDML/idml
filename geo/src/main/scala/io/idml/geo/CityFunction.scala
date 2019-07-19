package io.idml.geo

import io.idml.datanodes.{IInt, IObject, IString}
import io.idml._
import io.idml.ast.Pipeline
import io.idml.functions.IdmlFunction1
import cats._, cats.implicits._, cats.syntax._, cats.effect._
import doobie.implicits._, doobie._
import doobie.hikari._, doobie.hikari.implicits._
import org.slf4j.LoggerFactory

class CityFunction(driver: String, url: String, user: String, password: String) {
  val log = LoggerFactory.getLogger(getClass)

  lazy val xa = HikariTransactor
    .newHikariTransactor[IO](
      driver,
      url,
      user,
      password
    )
    .unsafeRunSync()

  case class City(id: Long, name: String, asciiname: String) {
    def toPValue: IdmlValue = {
      IObject(
        "id"        -> IInt(id),
        "name"      -> IString(name),
        "asciiname" -> IString(asciiname)
      )
    }
  }

  def find(id: Long): ConnectionIO[Option[City]] =
    sql"select * from Cities where id = $id".query[City].option

  def get(id: Long): Option[IdmlValue] =
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

  case class CityFunction(arg: Pipeline) extends IdmlFunction1 {
    override protected def apply(cursor: IdmlValue, country: IdmlValue): IdmlValue = {
      country match {
        case i: IInt => get(i.value).getOrElse(MissingField)
        case _       => InvalidParameters
      }
    }

    override def name: String = "city"
  }
}
