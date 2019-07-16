package io.idml.server

import cats.effect.IO
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.circe._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser._
import cats._
import cats.syntax._
import cats.implicits._
import cats.data._
import cats.effect._
import io.idml.utils.{AnalysisModule, AutoComplete}
import io.idml.ast.PtolemyFunctionMetadata
import io.idml.hashing.HashingFunctionResolver
import io.idml.jsoup.JsoupFunctionResolver
import io.idml._
import fs2._
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame.Text
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import scala.collection.JavaConverters._
import scala.util.Try

object WebsocketServer {
  val log = LoggerFactory.getLogger(getClass)

  import org.http4s.dsl.io._
  import org.http4s.websocket._

  case class Request(in: List[Json], idml: String, path: Option[String])
  case class Response(out: Option[List[Json]], errors: Option[List[String]])

  case class Complete(in: List[Json], idml: String, position: Int)
  implicit def dec[F[_]: Sync, A](implicit decoder: Decoder[A]): EntityDecoder[F, A] = jsonOf

  val functions =
    (StaticFunctionResolverService.defaults.asScala ++ List(new JsoupFunctionResolver, new HashingFunctionResolver)).toList.flatMap { f =>
      f.providedFunctions().filterNot(_.name.startsWith("$"))
    }

  val completionPtolemy = new Ptolemy(
    new PtolemyConf(),
    new StaticFunctionResolverService(
      (StaticFunctionResolverService.defaults.asScala ++ List(new JsoupFunctionResolver, new HashingFunctionResolver, new AnalysisModule)).asJava
    )
  )

  def service(fr: FunctionResolverService)(implicit ec: ExecutionContext, c: Concurrent[IO]) = {
    val ptolemy = new Ptolemy(new PtolemyConf(), fr)
    HttpService[IO] {
      case GET -> Root / "functions" / partial =>
        Ok(functions.filter(_.name.startsWith(partial)).asJson)
      case req @ POST -> Root / "completion" =>
        req
          .as[Complete]
          .map { c =>
            c.in.flatMap { doc =>
              AutoComplete.complete(completionPtolemy)(jackson.PtolemyJson.parse(doc.noSpaces).asInstanceOf[PtolemyObject], c.idml, c.position)
            }.asJson
          }
          .flatMap(Ok(_))

      case req @ GET -> Root =>
        val queue = fs2.concurrent.Queue.unbounded[IO, WebSocketFrame]
        val echoReply: Pipe[IO, WebSocketFrame, WebSocketFrame] = _.collect {
          case Text(msg, _) => Text("You sent the server: " + msg)
          case _            => Text("Something new")
        }
        val main: Pipe[IO, WebSocketFrame, Response] = _.flatMap {
          _ match {
            case Text(msg, _) =>
              Stream.eval(
                IO {
                  parse(msg)
                    .map(_.as[Request])
                    .leftMap { e =>
                      List(e.message)
                    }
                    .map { r =>
                      r.toTry.toEither.leftMap(e => List(e.getMessage))
                    }
                    .flatMap(identity)
                    .flatMap { x =>
                      Try {
                        log.info(x.toString)
                        val chain = ptolemy.fromString(x.idml)
                        val jsons = x.in.map(i => jackson.PtolemyJson.parse(i.toString))
                        val p     = x.path.getOrElse("root")
                        val path  = ptolemy.fromString(s"result = $p")
                        jsons
                          .map { j =>
                            parse(jackson.PtolemyJson.compact(path.run(chain.run(j)))).toTry
                          }
                          .sequence
                          .toEither
                      }.toEither
                        .leftMap(e => List(e.getMessage))
                        .flatMap(_.toTry.toEither.leftMap(e => List(e.getMessage)))
                    }
                    .map(result => Response(Some(result), None))
                    .leftMap(e => Response(None, Some(e)))
                    .merge
                }
              )
            case _ => Stream.emit(Response(None, Some(List("Please send your request as Text")))).covary[IO]
          }
        }

        queue.flatMap { q =>
          val d = q.dequeue.through(main.andThen(_.map(resp => Text(resp.asJson.toString()))))
          val e = q.enqueue
          WebSocketBuilder[IO].build(d, e)
        }

    }
  }

}
