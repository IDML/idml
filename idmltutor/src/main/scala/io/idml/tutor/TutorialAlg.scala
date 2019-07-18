package io.idml.tutor

import cats._
import cats.data.EitherT
import cats.implicits._
import cats.effect._
import cats.effect.implicits._
import fansi._
import io.idml.{Idml, IdmlValue}
import Colours._
import io.idml.test.diffable.TestDiff
import io.circe._, io.circe.syntax._
import io.idml.circe.instances._

class TutorialAlg[F[_]](jline: JLine[F])(implicit F: Sync[F]) {
  def title(s: String): F[Unit] =
    for {
      width <- jline.width()
      _     <- jline.printAbove(Underlined.On(Utils.center(s, width, s.length)).render)
    } yield ()
  def section(s: String): F[Unit] =
    for {
      width <- jline.width()
      _     <- jline.printAbove(Utils.center(s, width, s.length))
    } yield ()

  def content(s: String): F[Unit] = jline.printAbove(s)

  val pause: F[String] = jline.readLine("[Enter to continue]")
  def exercise(body: String, in: Json, out: Json, multiline: Boolean = false): F[Unit] =
    exerciseMulti(body, List((in, out)), multiline)
  def exerciseMulti(body: String, pairs: List[(Json, Json)], multiline: Boolean = false): F[Unit] =
    for {
      engine <- F.delay {
                 Idml.createAuto(_.build())
               }
      pvs = pairs.map {
        case (k, v) =>
          (
            k.as[IdmlValue].toOption.get,
            v.as[IdmlValue].toOption.get
          )
      }
      rendered = pairs.map {
        case (k, v) =>
          s"""
            |Input:
            |${grey(k.spaces2)}
            |Desired Output:
            |${grey(v.spaces2)}
          """.stripMargin
      }.mkString
      _ <- F.unit.whileM_({
            for {
              _ <- EitherT.liftF[F, Boolean, Unit](jline.printAbove(s"""$body
              |$rendered
              """.stripMargin))
              idml <- EitherT.liftF[F, Boolean, String](
                       if (multiline)
                         jline.readMultiline(grey("~> "))
                       else
                         jline.readLine(grey("~> "))
                     )
              compiled <- F.delay {
                             engine.compile(idml)
                           }
                           .attemptT
                           .leftSemiflatMap { t =>
                             jline.printAbove(red(s"Error: ${t.getMessage}")).as(true)
                           }
              outputs <- pvs.traverse {
                          case (input, _) =>
                            F.delay {
                                compiled.run(input)
                              }
                              .attemptT
                              .leftSemiflatMap { t =>
                                jline.printAbove(red(s"Error: ${t.getMessage}")).as(true)
                              }
                        }
              _ <- outputs.zip(pvs.map(_._2)).traverse {
                    case (output, desiredOutput) =>
                      if (!output.equals(desiredOutput)) {
                        EitherT.left[Unit] {
                          jline
                            .printAbove(
                              s"""${red("Output differs:")}

                     |${TestDiff.generateDiff(output.asJson, desiredOutput.asJson)}
                 """.stripMargin
                            )
                            .as(true)
                        }
                      } else {
                        EitherT.rightT[F, Boolean](())
                      }
                  }
              _ <- EitherT.liftF[F, Boolean, Unit](
                    jline.printAbove(green("Correct"))
                  )
            } yield false
          }.merge)
    } yield ()

}
