package io.idml.test

import io.circe._
import cats._
import cats.implicits._
import cats.data._
import cats.effect._

final case class Tests(tests: List[Test])
object Tests {
  implicit def encoder(implicit t: Encoder[Test]): Encoder[Tests] =
    (a: Tests) =>
      a.tests match {
        case ts @ Nil    => Encoder[List[Test]].apply(ts)
        case head :: Nil => Encoder[Test].apply(head)
        case ts          => Encoder[List[Test]].apply(ts)
    }
  implicit def decoder(implicit t: Decoder[Test]): Decoder[Tests] =
    (c: HCursor) =>
      c.focus match {
        case Some(focus) if focus.isArray => Decoder[List[Test]].map(Tests.apply).apply(c)
        case _                            => Decoder[Test].map(t => Tests(List(t))).apply(c)
    }

}

final case class Ref(`$ref`: String)
final case class Test(
    name: String,
    code: Either[Ref, String],
    input: Either[Ref, Json],
    output: Either[Ref, Json],
    time: Option[Long]
) {
  def reportErrorWithRef[F[_]: Sync, T](r: Ref, f: F[T]): F[T] =
    f.attempt.map(_.leftMap(e => new Throwable(s"Unable to load reference to ${r.`$ref`}: ${e.getMessage}", e))).rethrow
  def resolve[F[_]: Sync](load: Ref => F[String], parse: Ref => F[Json]): F[ResolvedTest] = {
    (
      code.swap.traverse(r => reportErrorWithRef(r, load(r))).map(_.merge),
      input.swap.traverse(r => reportErrorWithRef(r, parse(r))).map(_.merge),
      output.swap.traverse(r => reportErrorWithRef(r, parse(r))).map(_.merge)
    ).mapN {
      case (c, i, o) =>
        ResolvedTest(
          name,
          c,
          i,
          o,
          time
        )
    }
  }
  def updateResolve[F[_]: Sync](load: Ref => F[String], parse: Ref => F[Json]): F[UpdateableResolvedTest] = {
    (
      code.swap.traverse(r => reportErrorWithRef(r, load(r))).map(_.merge),
      input.swap.traverse(r => reportErrorWithRef(r, parse(r))).map(_.merge),
    ).mapN {
      case (c, i) =>
        UpdateableResolvedTest(
          this,
          name,
          c,
          i,
          output,
          time
        )
    }
  }
}

final case class ResolvedTest(
    name: String,
    code: String,
    input: Json,
    output: Json,
    time: Option[Long],
)

final case class UpdateableResolvedTest(
    original: Test,
    name: String,
    code: String,
    input: Json,
    output: Either[Ref, Json],
    time: Option[Long],
)
