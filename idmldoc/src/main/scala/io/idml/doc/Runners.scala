package io.idml.doc

import cats.effect._
import cats.effect.implicits._
import cats._
import cats.effect.concurrent.Ref
import cats.implicits._
import io.idml.circe.IdmlCirce
import io.idml.datanodes.IObject
import io.idml.doc.Markdown.{Code, Node, Text}
import io.idml._
import io.idml.utils.Tracer.Annotator

object Runners {

  trait Runner[F[_]] {
    def run(block: Code): F[List[Node]]
  }

  def run[F[_]: Monad: Applicative: Effect](markdown: List[Node]): F[List[Node]] =
    idmlRunner[F].flatMap { r =>
      markdown
        .traverse {
          case c: Code => r.run(c)
          case n: Node => List(n).pure[F]
        }
        .map(_.flatten)
    }

  def idmlRunner[F[_]: Monad: Applicative](implicit F: Effect[F]): F[Runner[F]] =
    for {
      idml  <- F.delay { Idml.autoBuilder().build() }
      input <- Ref[F].of(IdmlJson.newObject())
      code  <- Ref[F].of(idml.compile(""))
    } yield new Runner[F] {
      override def run(block: Code): F[List[Node]] = {
        block match {
          case b @ Code(label, content) =>
            label.split(":").toList match {
              case "idml" :: "input" :: modes =>
                F.delay { IdmlCirce.parse(content) }.flatMap {
                  case o: IObject => input.set(o)
                  case _          => F.unit
                } *> F.pure(
                  if (modes.contains("silent")) List.empty
                  else List(Code("json", content))
                )
              case "idml" :: "code" :: modes  =>
                F.delay { idml.compile(content) }.flatMap { m =>
                  code.set(m)
                } *> {
                  modes match {
                    case m if m.contains("silent") =>
                      F.pure(List(Code("idml", content)))
                    case m if m.contains("inline") =>
                      (code.get, input.get).tupled.flatMap { case (c, i) =>
                        F.delay {
                          val a   = new Annotator(IdmlCirce)
                          val ctx = new IdmlContext(i, IdmlJson.newObject(), List[IdmlListener](a))
                          c.run(ctx)
                          List(Code("idml", a.render(content)))
                        }
                      }
                    case _                         =>
                      (code.get, input.get).bisequence
                        .map { case (c, i) => IdmlCirce.pretty(c.run(i)) }
                        .map { output =>
                          List(Code("idml", content), Text("\n"), Code("json", output))
                        }
                  }
                }
              case _                          => F.pure(List(b))
            }
        }
      }
    }

}
