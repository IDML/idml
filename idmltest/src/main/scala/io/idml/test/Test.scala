package io.idml.test

import io.circe._
import cats._
import cats.implicits._
import cats.data._
import cats.effect._

import Test._

final case class Tests[T](tests: List[ParsedTest[T]])
object Tests {
  implicit def encoder[T](implicit t: Encoder[ParsedTest[T]]): Encoder[Tests[T]] =
    (a: Tests[T]) =>
      a.tests match {
        case ts @ Nil    => Encoder[List[ParsedTest[T]]].apply(ts)
        case head :: Nil => Encoder[ParsedTest[T]].apply(head)
        case ts          => Encoder[List[ParsedTest[T]]].apply(ts)
      }
  implicit def decoder[T](implicit t: Decoder[ParsedTest[T]]): Decoder[Tests[T]] =
    (c: HCursor) =>
      c.focus match {
        case Some(focus) if focus.isArray => Decoder[List[ParsedTest[T]]].map(Tests.apply).apply(c)
        case _                            => Decoder[ParsedTest[T]].map(t => Tests(List(t))).apply(c)
      }

}

final case class PipelinedCode[T](
    pipeline: String,
    database: Map[String, T]
)
object PipelinedCode {
  implicit val functor: Functor[PipelinedCode]                = new Functor[PipelinedCode] {
    override def map[A, B](fa: PipelinedCode[A])(f: A => B): PipelinedCode[B] =
      fa.copy(database = fa.database.mapValues(f).toMap)
  }
  implicit val pipelinedCodeTraverse: Traverse[PipelinedCode] = new Traverse[PipelinedCode] {
    override def traverse[G[_], A, B](fa: PipelinedCode[A])(f: A => G[B])(implicit
        evidence$1: Applicative[G]): G[PipelinedCode[B]] =
      fa.database.toList
        .traverse { case (k, v) =>
          f(v).tupleLeft(k)
        }
        .map { d =>
          fa.copy(database = d.toMap)
        }
    override def foldLeft[A, B](fa: PipelinedCode[A], b: B)(f: (B, A) => B): B =
      fa.database.values.foldLeft(b)(f)
    override def foldRight[A, B](fa: PipelinedCode[A], lb: Eval[B])(
        f: (A, Eval[B]) => Eval[B]): Eval[B] =
      fa.database.values.foldRight(lb)(f)
  }
}
final case class Ref(`$ref`: String)
final case class Test[F[_], F2[_], T](
    name: String,
    code: F[Either[String, PipelinedCode[F[String]]]],
    input: F[T],
    output: F2[T],
    time: Option[Long],
    original: Option[ParsedTest[T]] = None
)

object Test {
  type ParsedTest[T]    = Test[Either[Ref, ?], Either[Ref, ?], T]
  type UpdatableTest[T] = Test[Id, Either[Ref, ?], T]
  type ResolvedTest[T]  = Test[Id, Id, T]

  type ParsedSingleTest    = ParsedTest[Json]
  type UpdatableSingleTest = UpdatableTest[Json]
  type ResolvedSingleTest  = ResolvedTest[Json]

  type ParsedMultiTest    = ParsedTest[List[Json]]
  type UpdatableMultiTest = UpdatableTest[List[Json]]
  type ResolvedMultiTest  = ResolvedTest[List[Json]]

  def reportErrorWithRef[F[_]: Sync, T](r: Ref, f: F[T]): F[T] =
    f.attempt
      .map(_.leftMap(e =>
        new Throwable(s"Unable to load reference to ${r.`$ref`}: ${e.getMessage}", e)))
      .rethrow

  def decodeRaise[F[_]: Sync, T: Decoder](j: Json): F[T] =
    Sync[F].fromEither(Decoder[T].decodeJson(j))

  implicit class ResolveSyntax[T: Decoder](t: ParsedTest[T]) {
    def resolve[F[_]: Sync](
        load: Ref => F[String],
        parse: String => F[Json]): F[ResolvedTest[T]] = {
      val reportAndLoad = (r: Ref) => reportErrorWithRef(r, load(r))
      (
        t.code
          .leftTraverse(reportAndLoad)
          .flatMap(_.traverse(_.traverse(_.traverse(_.leftTraverse(reportAndLoad).map(_.merge))))
            .map(_.flatten)),
        t.input.swap
          .traverse(r => reportErrorWithRef(r, load(r).flatMap(parse).flatMap(decodeRaise[F, T])))
          .map(_.merge),
        t.output.swap
          .traverse(r => reportErrorWithRef(r, load(r).flatMap(parse).flatMap(decodeRaise[F, T])))
          .map(_.merge)
      ).mapN { case (c, i, o) =>
        Test[Id, Id, T](
          t.name,
          c,
          i,
          o,
          t.time
        )
      }
    }

    def updateResolve[F[_]: Sync](
        load: Ref => F[String],
        parse: String => F[Json]): F[UpdatableTest[T]] = {
      val reportAndLoad = (r: Ref) => reportErrorWithRef(r, load(r))
      (
        t.code
          .leftTraverse(reportAndLoad)
          .flatMap(_.traverse(_.traverse(_.traverse(_.leftTraverse(reportAndLoad).map(_.merge))))
            .map(_.flatten)),
        t.input.swap
          .traverse(r => reportErrorWithRef(r, load(r).flatMap(parse).flatMap(decodeRaise[F, T])))
          .map(_.merge)
      ).mapN { case (c, i) =>
        Test[Id, Either[Ref, ?], T](
          t.name,
          c,
          i,
          t.output,
          t.time,
          Some(t)
        )
      }
    }
  }
}
