package io.idml.test
import io.circe.{Decoder, Encoder, Json}
import cats._, cats.implicits._

trait CirceEitherEncoders {
  implicit def goodEitherDecoder[A, B](implicit a: Decoder[A], b: Decoder[B]): Decoder[Either[A, B]] = {
    a.map(_.asLeft[B]) or b.map(_.asRight[A])
  }

  implicit def goodEitherEncoder[A, B](implicit a: Encoder[A], b: Encoder[B]): Encoder[Either[A, B]] =
    (e: Either[A, B]) => e.bimap(a.apply, b.apply).merge
}
