package io.idml.circe

import io.circe.Json.Folder
import io.circe._
import io.circe.syntax._
import io.idml.datanodes._

import scala.collection.mutable
import scala.util.Try
import cats._, cats.implicits._

import io.idml._

class CirceJson extends PtolemyJson {
  import io.idml.circe.instances._

  def read(in: String): Either[Throwable, PtolemyValue] = io.circe.parser.decode[PtolemyValue](in).leftMap[Throwable](e => new PtolemyJsonReadingException(e))

  /** Take a json string and transform it into a DataNode hierarchy */
  override def parse(in: String): PtolemyValue = read(in).toTry.get

  /** Take a json string and transform it into a DataNode hierarchy, if it's an object */
  override def parseObject(in: String): PtolemyObject = read(in).flatMap {
    case o: PtolemyObject => o.asRight
    case _ => (new PtolemyJsonObjectException).asLeft
  }.toTry.get

  /** Render a DataNode hierarchy as compacted json */
  override def compact(d: PtolemyValue): String = d.asJson.noSpaces

  /** Render a DataNode hierarchy as pretty-printed json */
  override def pretty(d: PtolemyValue): String = d.asJson.spaces4
}

object CirceJson extends CirceJson
