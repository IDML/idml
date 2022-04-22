package io.idml.test.diffable
import io.circe.Json

import atto._, Atto._
import cats._, cats.implicits._

object DiffableParser {

  lazy val json: Parser[Json] = jstring | jnull | jnumber | jarray | jobject
  val jstring                 = stringLiteral.map(Json.fromString)
  val jnull                   = string("null").map(_ => Json.Null)
  val jnumber                 = bigDecimal.map(Json.fromBigDecimal)
  val jarray                  = for {
    _     <- char('[') <* skipWhitespace
    items <- (skipWhitespace *> json <* skipWhitespace <* opt(char(',')) <* skipWhitespace).many
    _     <- char(']') <* skipWhitespace
  } yield Json.arr(items: _*)

  val jobjectkv: Parser[(String, Json)] = for {
    key   <- stringLiteral <* skipWhitespace
    _     <- char(':') <* skipWhitespace
    value <- json <* skipWhitespace
    _     <- opt(char(',') <* skipWhitespace)
  } yield key -> value

  val jobject = for {
    _   <- char('{') <* skipWhitespace
    kvs <- jobjectkv.many
    _   <- char('}') <* skipWhitespace
  } yield Json.obj(kvs: _*)

  def parse(s: String): ParseResult[Json] = json.parseOnly(s).done
}
