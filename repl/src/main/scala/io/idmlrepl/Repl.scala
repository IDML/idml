package io.idmlrepl

import java.nio.file.{Files, Paths}
import cats._
import cats.data.EitherT
import cats.effect.{Blocker, ContextShift, ExitCode, Sync}
import cats.implicits._
import cats.mtl._
import cats.mtl.implicits._
import io.circe.Json
import io.idml.circe.IdmlCirce
import io.idml.{IdmlMapping, IdmlObject, Mapping}
import org.jline.reader.EOFError
import cats.tagless._
import fansi.Color.LightGray
import fs2.io.file.readAll

import scala.concurrent.ExecutionContext

// These represent the two modes the REPL can be in
sealed trait ReplMode
object JSONMode extends ReplMode
object IDMLMode extends ReplMode

// This represents the results of attempting to read a line from the repl, this encapsulates all the things jline can return
sealed trait ReadLine[T]
case class Result[T](t: T)             extends ReadLine[T]
case class Error[T](e: Throwable)      extends ReadLine[T]
case class Command[T](command: String) extends ReadLine[T]
case class EOF[T]()                    extends ReadLine[T]

// The tagless API for the jline implementation of the terminal
trait JLine[F[_]] {
  def printAbove(s: String): F[Unit]
  def readJson: F[ReadLine[IdmlObject]]
  def readIdml: F[ReadLine[Mapping]]
  def compileJson: String => Either[Throwable, IdmlObject]
  def compileIdml: String => Either[Throwable, Mapping]
  def close: F[Unit]
}
object JLine      {
  implicit def functorKForJLine: FunctorK[JLine] = Derive.functorK[JLine]
}

// The pure implementation of the REPL logic
class Repl[F[_]: Sync: ContextShift](jline: JLine[F], blocker: Blocker)(implicit
    mode: MonadState[F, ReplMode],
    data: MonadState[F, IdmlObject],
    exit: FunctorRaise[F, ExitCode]
) {
  object Load {
    def unapply(s: String): Option[String] =
      if (s.startsWith(".load "))
        Some(s.stripPrefix(".load "))
      else None
  }

  def loadFile(s: String): F[String] =
    for {
      p    <- Sync[F].delay { Paths.get(s) }
      body <- readAll[F](p, blocker, 1024).through(fs2.text.utf8Decode[F]).compile.foldMonoid
    } yield body

  def command(s: String): F[Unit] =
    s match {
      case ".json"           => mode.set(JSONMode)
      case ".idml"           => mode.set(IDMLMode)
      case ".exit" | ".quit" => exit.raise[Unit](ExitCode.Success)
      case ".help"           =>
        jline.printAbove(
          s"""This REPL consists of two modes: json and idml. The current mode is identified by the prompt.
        |To switch modes you can use the ${LightGray(".idml")} or ${LightGray(".json")} commands.
        |
        |In JSON mode you are entering a single JSON object with which to work, you may use the ${LightGray(
              ".load")}
        |command to bring in an external file containing a JSON object.
        |
        |In IDML mode you are entering a mapping and must terminate it with a double newline. You can again use
        |${LightGray(".load")} to load in a file.
        |
        |The full set of commands are as follows:
        |
        |  .help            Display this help message
        |  .exit or .quit   Exit the REPL
        |  .json            Switch to JSON input mode
        |  .idml            Switch to IDML input mode
        |  .load <filename> Load the target file as your current input
        |""".stripMargin)
      case Load(file)        =>
        for {
          m <- mode.get
          _ <- loadFile(file).attemptT
                 .flatMap { contents =>
                   m match {
                     case JSONMode =>
                       EitherT(Sync[F].delay {
                         jline.compileJson(contents)
                       }).semiflatMap(data.set(_) *> jline.printAbove(
                         s"$file loaded as JSON") *> mode.set(IDMLMode))
                     case IDMLMode =>
                       EitherT(Sync[F].delay {
                         jline.compileIdml(contents)
                       }).semiflatMap { m =>
                         jline.printAbove(s"running $file as IDML") *> data.get
                           .map(m.run)
                           .map(IdmlCirce.pretty)
                           .flatMap(jline.printAbove)
                       }
                   }
                 }
                 .leftSemiflatMap(e => jline.printAbove(e.toString))
                 .value
        } yield ()
      case _                 => jline.printAbove(s"unknown command: $s")
    }

  def process[T](read: F[ReadLine[T]])(f: T => F[Unit]): F[Unit] =
    read.flatMap {
      case Result(t)  => f(t)
      case Error(e)   => jline.printAbove(e.toString)
      case Command(c) => command(c)
      case EOF()      => exit.raise[Unit](ExitCode.Success)
    }

  // the most important method of the REPL, this is run repeatedly, since it uses and modifies state and can raise an ExitCode
  def step: F[Unit] =
    for {
      m <- mode.get
      _ <- m match {
             case JSONMode =>
               process(jline.readJson) { j =>
                 data.set(j) *> mode.set(IDMLMode)
               }
             case IDMLMode =>
               process(jline.readIdml) { m =>
                 data.get.map(m.run).map(IdmlCirce.pretty).flatMap(jline.printAbove)
               }
           }
    } yield ()
}
