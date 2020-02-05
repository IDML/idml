package io.idmlrepl

import cats._
import cats.data._
import cats.effect.concurrent.Ref
import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp}
import cats.mtl.implicits._
import cats.tagless.implicits._
import io.idml.{IdmlObject}
import io.idml.datanodes.IObject
import io.idml.BuildInfo
import scala.concurrent.ExecutionContext.Implicits.global

object Main extends IOApp {
  type ReplMonad[T] = EitherT[StateT[StateT[IO, ReplMode, ?], IdmlObject, ?], ExitCode, T]
  val f: IO ~> ReplMonad = StateT.liftK[IO, ReplMode] andThen
    StateT.liftK[StateT[IO, ReplMode, ?], IdmlObject] andThen
    EitherT.liftK[StateT[StateT[IO, ReplMode, ?], IdmlObject, ?], ExitCode]

  override def run(args: List[String]): IO[ExitCode] =
    for {
      data     <- Ref.of[IO, IdmlObject](IObject())
      jline    <- IO { new JLineImpl[IO](data) }.widen[JLine[IO]]
      jliner   = jline.mapK(f)
      repl     = new Repl(jliner)
      _        <- jline.printAbove(s"""idml ${BuildInfo.version} (${BuildInfo.builtAtString})
         |
         |Type ".help" for instructions on how to use this tool. Press ctrl+c, ctl+d or type .exit to exit.""".stripMargin)
      exitCode <- repl.step.foreverM.value.runA(IObject()).runA(JSONMode)
    } yield exitCode.left.getOrElse(ExitCode.Success)
}
