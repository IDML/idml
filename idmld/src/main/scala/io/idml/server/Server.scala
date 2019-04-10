package io.idml.server

import cats.effect._
import io.idml.FunctionResolverService
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext.Implicits.global

object Server extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    BlazeBuilder[IO]
      .mountService(WebsocketServer.service(new FunctionResolverService), "/")
      .bindHttp(8081, "localhost")
      .serve
      .compile
      .lastOrError
}
