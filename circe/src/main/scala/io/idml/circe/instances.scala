package io.idml.circe

import io.circe.Json.Folder
import io.circe._
import io.circe.syntax._
import io.idml.datanodes._

import scala.collection.mutable
import scala.util.Try
import cats._, cats.implicits._

import io.idml._

/**
  * Encoder and Decoder instances for Ptolemy types
  */
object instances {

  lazy val rawIdmlCirceDecoder: Folder[PtolemyValue] = new Folder[PtolemyValue] {
    override def onNull: PtolemyValue                    = PtolemyNull
    override def onBoolean(value: Boolean): PtolemyValue = PBool(value)
    override def onNumber(value: JsonNumber): PtolemyValue =
      value.toLong.fold[PtolemyValue](PDouble(value.toDouble))(l => PInt(l))
    override def onString(value: String): PtolemyValue = PString(value)
    override def onArray(value: Vector[Json]): PtolemyValue = new PArray(
      value.map(_.foldWith(rawIdmlCirceDecoder)).toBuffer
    )
    override def onObject(value: JsonObject): PtolemyValue = decodeObject(value)
  }

  implicit val decoder: Decoder[PtolemyValue] =
    Decoder[Json].emapTry(o => Try(o.foldWith(rawIdmlCirceDecoder)))

  lazy val rawIdmlCirceEncoder: PtolemyValue => Json = {
    case n: PtolemyInt    => Json.fromLong(n.value)
    case n: PtolemyDouble => Json.fromDoubleOrNull(n.value)
    case n: PtolemyString => Json.fromString(n.value)
    case n: PtolemyBool   => Json.fromBoolean(n.value)

    case n: PtolemyArray =>
      Json.arr(n.items.filterNot(_.isInstanceOf[PtolemyNothing]).map(rawIdmlCirceEncoder): _*)
    case n: PtolemyObject =>
      Json.obj(
        n.fields.toList.filterNot(_._2.isInstanceOf[PtolemyNothing]).map {
          case (k, v) => k -> rawIdmlCirceEncoder(v)
        }: _*
      )
    case _: PtolemyNothing => Json.Null
    case PtolemyNull       => Json.Null
  }

  def decodeObject(value: JsonObject): PtolemyObject =
    new PObject(
      value.toIterable.foldLeft(mutable.SortedMap.empty[String, PtolemyValue]) {
        case (acc, (k, v)) => acc += k -> v.foldWith(rawIdmlCirceDecoder)
      }
    )

  implicit val objectDecoder: Decoder[PtolemyObject] =
    Decoder[Json].emap { j: Json =>
      Either.fromOption(j.asObject, "Only an Object can be decoded into a PtolemyObject")
    }.emapTry(o => Try(decodeObject(o)))

  implicit val idmlCirceEncoder: Encoder[PtolemyValue] = Encoder.instance(rawIdmlCirceEncoder)

  // and for PtolemyObject
  implicit val ptolemyObjectEncoder: Encoder[PtolemyObject] = idmlCirceEncoder.narrow[PtolemyObject]
  // and for PObject just for ease of use
  implicit val ptolemyPObjectEncoder: Encoder[PObject] = idmlCirceEncoder.narrow[PObject]


}