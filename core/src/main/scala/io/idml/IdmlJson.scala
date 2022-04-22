package io.idml

import java.util.ServiceLoader

import io.idml.datanodes.IObject
import cats._
import cats.implicits._

/** Interface for any JSON parser which can work with the IDML datanodes hierarchy
  */
trait IdmlJson {

  /** Take a json string and transform it into a DataNode hierarchy */
  @throws[IdmlJsonReadingException]("if the JSON doesn't parse")
  def parse(in: String): IdmlValue

  /* Scala-friendly version of parse which uses an Either */
  def parseEither(in: String): Either[IdmlJsonReadingException, IdmlValue] =
    Either.catchOnly[IdmlJsonReadingException](parse(in))

  /** Take a json string and transform it into a DataNode hierarchy, if it's an object */
  @throws[IdmlJsonReadingException]("if the JSON doesn't parse")
  @throws[IdmlJsonObjectException]("if it's not an object")
  def parseObject(in: String): IdmlObject

  /* Scala-friendly version of parseObject which uses an Either */
  def parseObjectEither(in: String): Either[IdmlJsonException, IdmlObject] =
    try {
      Right(parseObject(in))
    } catch {
      case e: IdmlJsonException =>
        Left(e)
    }

  /** Render a DataNode hierarchy as compacted json */
  def compact(d: IdmlValue): String

  /** Render a DataNode hierarchy as pretty-printed json */
  def pretty(d: IdmlValue): String
}

object IdmlJson {
  @throws[NoSuchElementException]("if there isn't an available implementation")
  def load(): IdmlJson     =
    ServiceLoader.load(classOf[IdmlJson], getClass.getClassLoader).iterator().next()
  def newObject(): IObject = IObject()
}

sealed abstract class IdmlJsonException(s: String) extends Throwable(s)

class IdmlJsonReadingException(underlying: Throwable)
    extends IdmlJsonException(s"Unable to parse json: ${underlying.getLocalizedMessage}") {
  this.initCause(underlying)
}

class IdmlJsonObjectException extends IdmlJsonException("JSON wasn't an object")
