package io.idml.test

import io.circe._
import cats._, cats.implicits._, cats.data._, cats.effect._

final case class Ref(`$ref`: String)
final case class Test(
    name: String,
    code: Either[Ref, String],
    input: Either[Ref, Json],
    output: Either[Ref, Json],
) {
  def resolve[F[_]: Sync](load: Ref => F[String], parse: Ref => F[Json]): F[ResolvedTest] = {
    (
      code.swap.traverse(load).map(_.merge),
      input.swap.traverse(parse).map(_.merge),
      output.swap.traverse(parse).map(_.merge)
    ).mapN {
      case (c, i, o) =>
        ResolvedTest(
          name,
          c,
          i,
          o,
        )
    }
  }
  def updateResolve[F[_]: Sync](load: Ref => F[String], parse: Ref => F[Json]): F[UpdateableResolvedTest] = {
    (
      code.swap.traverse(load).map(_.merge),
      input.swap.traverse(parse).map(_.merge),
    ).mapN {
      case (c, i) =>
        UpdateableResolvedTest(
          name,
          c,
          i,
          output,
        )
    }
  }
}

final case class ResolvedTest(
    name: String,
    code: String,
    input: Json,
    output: Json,
)

final case class UpdateableResolvedTest(
    name: String,
    code: String,
    input: Json,
    output: Either[Ref, Json],
)
