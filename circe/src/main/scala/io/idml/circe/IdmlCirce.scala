package io.idml.circe

import io.circe.Json.Folder
import io.circe._
import io.circe.syntax._
import io.idml.datanodes._

import scala.collection.mutable
import scala.util.Try
import cats._, cats.implicits._

import io.idml._

class IdmlCirce extends IdmlJson {
  import io.idml.circe.instances._

  def read(in: String): Either[Throwable, IdmlValue] =
    io.circe.parser.decode[IdmlValue](in).leftMap[Throwable](e => new IdmlJsonReadingException(e))

  /** Take a json string and transform it into a DataNode hierarchy */
  override def parse(in: String): IdmlValue = read(in).toTry.get

  /** Take a json string and transform it into a DataNode hierarchy, if it's an object */
  override def parseObject(in: String): IdmlObject =
    read(in)
      .flatMap {
        case o: IdmlObject => o.asRight
        case _             => (new IdmlJsonObjectException).asLeft
      }
      .toTry
      .get

  /** Render a DataNode hierarchy as compacted json */
  override def compact(d: IdmlValue): String = d.asJson.noSpaces

  /** Render a DataNode hierarchy as pretty-printed json */
  override def pretty(d: IdmlValue): String = d.asJson.spaces2
}

object IdmlCirce extends IdmlCirce
