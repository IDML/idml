package io.idml

import java.util.ServiceLoader

import io.idml.datanodes.PObject
import cats._
import cats.implicits._

/**
  * Interface for any JSON parser which can work with the IDML datanodes hierarchy
  */
trait PtolemyJson {

  /** Take a json string and transform it into a DataNode hierarchy */
  @throws[PtolemyJsonReadingException]("if the JSON doesn't parse")
  def parse(in: String): PtolemyValue

  /* Scala-friendly version of parse which uses an Either */
  def parseEither(in: String): Either[PtolemyJsonReadingException, PtolemyValue] = Either.catchOnly[PtolemyJsonReadingException](parse(in))

  /** Take a json string and transform it into a DataNode hierarchy, if it's an object */
  @throws[PtolemyJsonReadingException]("if the JSON doesn't parse")
  @throws[PtolemyJsonObjectException]("if it's not an object")
  def parseObject(in: String): PtolemyObject

  /* Scala-friendly version of parseObject which uses an Either */
  def parseObjectEither(in: String): Either[PtolemyJsonException, PtolemyObject] =
    try {
      Right(parseObject(in))
    } catch {
      case e: PtolemyJsonException =>
        Left(e)
    }

  /** Render a DataNode hierarchy as compacted json */
  def compact(d: PtolemyValue): String

  /** Render a DataNode hierarchy as pretty-printed json */
  def pretty(d: PtolemyValue): String
}

object PtolemyJson {
  @throws[NoSuchElementException]("if there isn't an available implementation")
  def load(): PtolemyJson  = ServiceLoader.load(classOf[PtolemyJson]).iterator().next()
  def newObject(): PObject = PObject()
}

sealed trait PtolemyJsonException

class PtolemyJsonReadingException(underlying: Throwable)
    extends Throwable(s"Unable to parse json: ${underlying.getLocalizedMessage}")
    with PtolemyJsonException {
  this.initCause(underlying)
}

class PtolemyJsonObjectException extends Throwable("JSON wasn't an object") with PtolemyJsonException
