package io.idml.doc

import cats.effect._
import cats.effect.implicits._
import cats._
import cats.effect.concurrent.Ref
import cats.implicits._
import io.idml.datanodes.PObject
import io.idml.doc.Markdown.{Node, Code, Text}
import io.idml.{Ptolemy, PtolemyConf, PtolemyJson, PtolemyObject}

object Runners {

  trait Runner[F[_]] {
    def run(block: Code): F[List[Code]]
  }

  def run[F[_]: Monad: Applicative: Effect](markdown: List[Node]): F[List[Node]] = idmlRunner[F].flatMap { r =>
    markdown.traverse {
      case c: Code => r.run(c).asInstanceOf[F[List[Node]]]
      case n: Node => List(n).pure[F]
    }.map(_.flatten)
  }

  def idmlRunner[F[_]: Monad: Applicative](implicit F: Effect[F]): F[Runner[F]] =
    for {
      ptolemy <- F.delay { new Ptolemy(new PtolemyConf()) }
      input   <- Ref[F].of(PtolemyJson.newObject())
      code    <- Ref[F].of(ptolemy.fromString(""))
    } yield
      new Runner[F] {
        override def run(block: Code): F[List[Code]] = {
          block match {
            case b @ Code(label, content) =>
              label.split(":").toList match {
                case "idml" :: "input" :: modes =>
                  F.delay { PtolemyJson.parse(content) }.flatMap {
                    case o: PObject => input.set(o)
                    case _          => F.unit
                  } *> F.pure(List(Code("json", content)))
                case "idml" :: "code" :: modes =>
                  F.delay { ptolemy.fromString(content) }.flatMap { m =>
                    code.set(m)
                  } *> {
                    modes match {
                      case m if m.contains("silent") =>
                        F.pure(List(Code("idml", content)))
                      case _ =>
                        (code.get, input.get).bisequence.map { case (c, i) => PtolemyJson.pretty(c.run(i)) }.map { output =>
                          List(Code("idml", content), Code("json", output))
                        }
                    }
                  }
                case _ => F.pure(List(b))
              }
          }
        }
      }

}
