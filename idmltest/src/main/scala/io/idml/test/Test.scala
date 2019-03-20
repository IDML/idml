package io.idml.test

import io.circe._
import io.circe.generic.auto._
import cats._, cats.implicits._, cats.data._, cats.effect._

final case class Ref(`$ref`: String)
final case class Test(
    name: String,
    code: Either[Ref, String],
    input: Either[Ref, Json],
    output: Either[Ref, Json],
    asserts: Option[List[String]]
) {
  def resolve[F[_]: Sync](load: Ref => F[String], parse: Ref => F[Json]): F[ResolvedTest] = {
    (
      code.swap.traverse(load).map(_.merge),
      input.swap.traverse(parse).map(_.merge),
      output.swap.traverse(parse).map(_.merge)
    ).mapN { case (c, i, o) =>
      ResolvedTest(
        name,
        c,
        i,
        o,
        asserts
      )
    }
  }
}

final case class ResolvedTest(
    name: String,
    code: String,
    input: Json,
    output: Json,
    asserts: Option[List[String]]
)

object Test {
  implicit val enc: Encoder[Test] = implicitly
  implicit val dec: Decoder[Test] = implicitly
}