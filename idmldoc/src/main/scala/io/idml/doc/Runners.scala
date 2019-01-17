package io.idml.doc

import cats.effect._
import cats.effect.implicits._
import cats._
import cats.effect.concurrent.Ref
import cats.implicits._
import io.idml.datanodes.PObject
import io.idml.{Ptolemy, PtolemyConf, PtolemyJson, PtolemyObject}
import org.commonmark.node.{AbstractVisitor, FencedCodeBlock}
import org.commonmark.parser.Parser

object Runners {

  case class FencedBlock(label: String, content: String, extra: Option[FencedBlock] = None)

  trait Runner[F[_]] {
    def run(block: FencedBlock): F[FencedBlock]
  }

  def idmlRunner[F[_]: Monad: Applicative](implicit F: Effect[F]): F[Runner[F]] =
    for {
      ptolemy <- F.delay { new Ptolemy(new PtolemyConf()) }
      input   <- Ref[F].of(PtolemyJson.newObject())
      code    <- Ref[F].of(ptolemy.fromString(""))
    } yield
      new Runner[F] {
        override def run(block: FencedBlock): F[FencedBlock] = {
          block match {
            case b @ FencedBlock(label, content, _) =>
              label.split(":").toList match {
                case "idml" :: "input" :: modes =>
                  F.delay { PtolemyJson.parse(content) }.flatMap {
                    case o: PObject => input.set(o)
                    case _          => F.unit
                  } *> F.pure(FencedBlock("json", content))
                case "idml" :: "code" :: modes =>
                  F.delay { ptolemy.fromString(content) }.flatMap { m =>
                    code.set(m)
                  } *> {
                    modes match {
                      case m if m.contains("silent") =>
                        F.pure(FencedBlock("idml", content))
                      case _ =>
                        (code.get, input.get).bisequence.map { case (c, i) => PtolemyJson.pretty(c.run(i)) }.map { output =>
                          FencedBlock("idml", content, extra = Some(FencedBlock("json", output)))
                        }
                    }
                  }
                case _ => F.pure(b)
              }
          }
        }
      }

  def runnerRunner[F[_]: Effect](r: Runner[F]) = new AbstractVisitor {
    override def visit(fencedCodeBlock: FencedCodeBlock): Unit = {
      val label   = fencedCodeBlock.getInfo
      val content = fencedCodeBlock.getLiteral
      val result  = Effect[F].toIO(r.run(FencedBlock(label, content))).unsafeRunSync()
      fencedCodeBlock.setInfo(result.label)
      fencedCodeBlock.setLiteral(result.content)
      result.extra.foreach { e =>
        val f = new FencedCodeBlock
        f.setInfo(e.label)
        f.setLiteral(e.content)
        fencedCodeBlock.appendChild(f)
      }
    }
  }
}
