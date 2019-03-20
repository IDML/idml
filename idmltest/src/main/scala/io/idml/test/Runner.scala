package io.idml.test
import java.io.File
import java.nio.file.{Path, Paths}

import cats._
import cats.implicits._
import cats.effect._
import io.circe.parser._
import io.circe.{Decoder, Json}
import fs2._

import scala.util.Try

class TestUtils[F[_]: Sync] {
  def readAll(p: Path): F[String] = fs2.io.file.readAll[F](p, 2048).through(fs2.text.utf8Decode[F]).compile.foldMonoid
  def parse(s: String): F[Json] = Sync[F].fromEither(_root_.io.circe.parser.parse(s))
  def as[T: Decoder](j: Json): F[T] = Sync[F].fromEither(j.as[T])
  def refToPath(r: Ref): F[Path] = Sync[F].fromTry(Try { Paths.get(r.`$ref`) })
}

object Runner extends TestUtils[IO] {
  case class State(folder: Path)

  def resolveRef(path: Path, ref: Ref): IO[Either[String, Path]] = IO { Paths.get(path.toString, ref.`$ref`) }.attempt.map(_.leftMap { e =>
    e.getMessage
  })

  def load(test: Path) = readAll(test).flatMap(parse).flatMap(as[Test]).tupleLeft(test)
  def resolve(path: Path, test: Test): IO[ResolvedTest] = test.resolve(
    refToPath(_).flatMap(readAll),
    refToPath(_).flatMap(readAll).flatMap(parse)
  )


}
