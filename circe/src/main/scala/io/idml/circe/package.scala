package io.idml

import io.circe.Json.Folder
import io.circe._
import io.circe.syntax._
import io.idml.datanodes._

import scala.collection.mutable
import scala.util.Try
import cats._, cats.implicits._

import io.idml._

package object circe {

  lazy val rawIdmlCirceDecoder: Folder[PtolemyValue] = new Folder[PtolemyValue] {
    override def onNull: PtolemyValue                    = PtolemyNull
    override def onBoolean(value: Boolean): PtolemyValue = PBool(value)
    override def onNumber(value: JsonNumber): PtolemyValue =
      value.toLong.fold[PtolemyValue](PDouble(value.toDouble))(l => PInt(l))
    override def onString(value: String): PtolemyValue = PString(value)
    override def onArray(value: Vector[Json]): PtolemyValue = new PArray(
      value.map(_.foldWith(rawIdmlCirceDecoder)).toBuffer
    )
    override def onObject(value: JsonObject): PtolemyValue = new PObject(
      value.toIterable.foldLeft(mutable.SortedMap.empty[String, PtolemyValue]) {
        case (acc, (k, v)) => acc += k -> v.foldWith(rawIdmlCirceDecoder)
      }
    )
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

  implicit val idmlCirceEncoder: Encoder[PtolemyValue] = Encoder.instance(rawIdmlCirceEncoder)

  // and for PtolemyObject
  implicit val ptolemyObjectEncoder: Encoder[PtolemyObject] = idmlCirceEncoder.narrow[PtolemyObject]
  // and for PObject just for ease of use
  implicit val ptolemyPObjectEncoder: Encoder[PObject] = idmlCirceEncoder.narrow[PObject]

  object CirceJson extends PtolemyJson {
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

}
