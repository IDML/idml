package io.idml.circe

import io.circe.Json.Folder
import io.circe._
import io.circe.syntax._
import io.idml.datanodes._

import scala.collection.mutable
import scala.util.Try
import cats._
import cats.implicits._
import io.circe.idml.utils.JsonNumberFolder
import io.idml._

/**
  * Encoder and Decoder instances for Idml types
  */
object instances {
  private def digitCounter(s: String): Int = s.toLowerCase.split('e').headOption.map(_.count(_.isDigit)).getOrElse(0)

  def loadNumber(value: JsonNumber): IdmlValue =
    JsonNumberFolder.fold(value)
  /*    value.toLong
      .map(IInt.apply)
      .widen[IdmlValue]
      .orElse(value.toBigInt.map(IBigInt.apply))
      .orElse(
        if (digitCounter(value.toString) > 15)
          value.toBigDecimal.map(IBigDecimal.apply)
        else
          None
      )
      .getOrElse(
        IDouble(value.toDouble)
      )
   */

  lazy val rawIdmlCirceDecoder: Folder[IdmlValue] = new Folder[IdmlValue] {
    override def onNull: IdmlValue                    = IdmlNull
    override def onBoolean(value: Boolean): IdmlValue = IBool(value)
    override def onNumber(value: JsonNumber): IdmlValue =
      loadNumber(value)
    override def onString(value: String): IdmlValue = IString(value)
    override def onArray(value: Vector[Json]): IdmlValue = new IArray(
      value.map(_.foldWith(rawIdmlCirceDecoder)).toBuffer
    )
    override def onObject(value: JsonObject): IdmlValue = decodeObject(value)
  }

  implicit val decoder: Decoder[IdmlValue] =
    Decoder[Json].emapTry(o => Try(o.foldWith(rawIdmlCirceDecoder)))

  lazy val rawIdmlCirceEncoder: IdmlValue => Json = {
    case n: IdmlInt        => Json.fromLong(n.value)
    case n: IdmlDouble     => Json.fromDoubleOrNull(n.value)
    case n: IdmlBigInt     => Json.fromBigInt(n.value)
    case n: IdmlBigDecimal => Json.fromBigDecimal(n.value)
    case n: IdmlString     => Json.fromString(n.value)
    case n: IdmlBool       => Json.fromBoolean(n.value)

    case n: IdmlArray =>
      Json.arr(n.items.filterNot(_.isInstanceOf[IdmlNothing]).map(rawIdmlCirceEncoder): _*)
    case n: IdmlObject =>
      Json.obj(
        n.fields.toList.filterNot(_._2.isInstanceOf[IdmlNothing]).sortBy(_._1).map {
          case (k, v) => k -> rawIdmlCirceEncoder(v)
        }: _*
      )
    case _: IdmlNothing => Json.Null
    case IdmlNull       => Json.Null
  }

  def decodeObject(value: JsonObject): IdmlObject =
    new IObject(
      value.toIterable.foldLeft(mutable.Map.empty[String, IdmlValue]) {
        case (acc, (k, v)) => acc += k -> v.foldWith(rawIdmlCirceDecoder)
      }
    )

  implicit val objectDecoder: Decoder[IdmlObject] =
    Decoder[Json]
      .emap { j: Json =>
        Either.fromOption(j.asObject, "Only an Object can be decoded into a IdmlObject")
      }
      .emapTry(o => Try(decodeObject(o)))

  implicit val idmlCirceEncoder: Encoder[IdmlValue] = Encoder.instance(rawIdmlCirceEncoder)

  // and for IdmlObject
  implicit val ptolemyObjectEncoder: Encoder[IdmlObject] = idmlCirceEncoder.narrow[IdmlObject]
  // and for PObject just for ease of use
  implicit val ptolemyPObjectEncoder: Encoder[IObject] = idmlCirceEncoder.narrow[IObject]

}
