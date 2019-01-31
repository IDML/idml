package io.idml.doc

import java.nio.file.{Files, Path}

import cats.effect._
import cats._
import cats.implicits._
import com.monovore.decline._
import fs2._

import scala.collection.JavaConverters._

object Main extends IOApp {
  object CLI {
    case class Run(in: Path, out: Path)
    val in  = Opts.argument[Path]("input")
    val out = Opts.argument[Path]("output")
    val program = Command(
      name = "idmldoc",
      header = "run IDML documentation from one folder into another",
    ) { (in, out).tupled }
  }

  def readFiles[F[_]: Sync](folder: Path): Stream[F, Path] =
    Stream.fromIterator[F, Path](Files.walk(folder).filter(_.toString.endsWith(".md")).iterator().asScala)

  def processFile[F[_]: Sync: Effect](in: Path, out: Path): F[Unit] =
    for {
      contents <- fs2.io.file.readAll(in, 2048).through(fs2.text.utf8Decode[F]).compile.foldMonoid
      _        <- Effect[F].delay { Files.createDirectories(out.getParent) }
      _        <- Effect[F].delay { println(s"Compiling $in into $out") }
      parsed   <- Effect[F].delay { Markdown.parse(contents).get.value }
      ran      <- Runners.run[F](parsed)
      output   <- Effect[F].delay { Markdown.render(ran) }
      _        <- Stream.emit(output).covary[F].through(fs2.text.utf8Encode[F]).to(fs2.io.file.writeAll(out)).compile.drain
    } yield ()

  def processFiles[F[_]: Sync: Effect](in: Path, out: Path): F[Unit] =
    readFiles[F](in)
      .evalMap { file =>
        processFile[F](file, out.resolve(in.relativize(file)))
      }
      .compile
      .drain

  override def run(args: List[String]): IO[ExitCode] =
    CLI.program
      .parse(args)
      .leftMap(h => IO { println(h); ExitCode.Error })
      .map {
        case (in, out) =>
          processFiles[IO](in, out).as(ExitCode.Success)
      }
      .bisequence
      .map(_.merge)
}
