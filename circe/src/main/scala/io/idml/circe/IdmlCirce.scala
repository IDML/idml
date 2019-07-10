package io.idml

import io.circe.Json.Folder
import io.circe._
import io.idml.datanodes._

import scala.collection.mutable
import scala.util.Try
import cats.implicits._

package object circe {

  lazy val rawIdmlCirceDecoder: Folder[PtolemyValue] = new Folder[PtolemyValue] {
    override def onNull: PtolemyValue                    = PtolemyNull
    override def onBoolean(value: Boolean): PtolemyValue = PBool(value)
    override def onNumber(value: JsonNumber): PtolemyValue =
      value.toLong
        .map { l =>
          PInt(l)
        }
        .getOrElse(
          PDouble(value.toDouble)
        )
    override def onString(value: String): PtolemyValue = PString(value)
    override def onArray(value: Vector[Json]): PtolemyValue = new PArray(
      value.map(_.foldWith(rawIdmlCirceDecoder)).toBuffer
    )
    override def onObject(value: JsonObject): PtolemyValue = {
      val m = mutable.Map.empty[String, PtolemyValue]
      value.toIterable.foreach {
        case (k, v) =>
          m.put(k, v.foldWith(rawIdmlCirceDecoder))
      }
      new PObject(m)
    }
  }

  implicit val decoder: Decoder[PtolemyValue] =
    Decoder.instance(c => Try { c.value.foldWith(rawIdmlCirceDecoder) }.toEither.leftMap(e => DecodingFailure.fromThrowable(e, List.empty)))

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

  implicit val idmlCirceEncoder: Encoder[PtolemyValue] = Encoder.instance(rawIdmlCirceEncoder)

  // and for PtolemyObject
  implicit val ptolemyObjectEncoder: Encoder[PtolemyObject] = idmlCirceEncoder.contramap(_.asInstanceOf[PtolemyValue])

  object PtolemyJson {
    def parse(in: String): Either[Error, PtolemyValue] = io.circe.parser.decode[PtolemyValue](in)
    def parseUnsafe(in: String): PtolemyValue          = io.circe.parser.decode[PtolemyValue](in).toTry.get
  }

}
