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
import io.idml.ast.IdmlFunctionMetadata
import io.idml.hashing.HashingFunctionResolver
import io.idml.jsoup.JsoupFunctionResolver
import io.idml._
import io.idml.circe.instances._
import io.idml.circe._
import io.idml.utils.Tracer.Annotator
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
  import org.http4s.circe.CirceEntityDecoder._

  case class Request(in: List[IdmlObject], idml: String, path: Option[String])
  case class Response(out: Option[List[Json]], errors: Option[List[String]], traced: Option[List[String]])

  case class Complete(in: List[IdmlObject], idml: String, position: Int)

  val functions =
    (StaticFunctionResolverService.defaults(IdmlCirce).asScala ++ List(new JsoupFunctionResolver, new HashingFunctionResolver)).toList
      .flatMap { f =>
        f.providedFunctions().filterNot(_.name.startsWith("$"))
      }

  val completionIdml =
    Idml
      .staticBuilderWithDefaults(IdmlCirce)
      .withResolver(new JsoupFunctionResolver)
      .withResolver(new HashingFunctionResolver)
      .withResolver(new AnalysisModule)
      .build()

  def service(fr: FunctionResolverService)(implicit ec: ExecutionContext, c: Concurrent[IO]) = {
    val idml = new IdmlBuilder(fr).build()
    HttpRoutes.of[IO] {
      case GET -> Root / "functions" / partial =>
        Ok(functions.filter(_.name.startsWith(partial)).asJson)
      case req @ POST -> Root / "completion" =>
        req
          .as[Complete]
          .map { c =>
            c.in.flatMap { doc =>
              AutoComplete.complete(completionIdml)(doc, c.idml, c.position)
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
                        val tracer = new Annotator(IdmlCirce)
                        val chain = idml.compile(x.idml)
                        val jsons = x.in
                        val p     = x.path.getOrElse("root")
                        val path  = idml.compile(s"result = $p")
                        jsons.traverse { j =>
                          Try {
                            val ctx = new IdmlContext(j)
                            ctx.setListeners(List[IdmlListener](tracer).asJava)
                            Mapping.fromMultipleMappings(List(path, chain))
                            val result = chain.run(ctx).output
                            val focusedResult = path.run(result)
                            (focusedResult.asJson, tracer.render(x.idml))
                          }.toEither
                        }
                      }.toEither
                        .leftMap(e => List(e.getMessage))
                        .flatMap(_.toTry.toEither.leftMap(e => List(e.getMessage)))
                    }
                    .map{ results =>
                      val (jsons, rendered) = results.separate
                      Response(Some(jsons), None, Some(rendered))
                    }
                    .leftMap(e => Response(None, Some(e), None))
                    .merge
                }
              )
            case _ => Stream.emit(Response(None, Some(List("Please send your request as Text")), None)).covary[IO]
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
