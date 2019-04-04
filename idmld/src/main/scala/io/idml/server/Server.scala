package io.idml.server

import cats.effect.IO
import fs2.StreamApp
import io.idml.FunctionResolverService
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext.Implicits.global

object Server extends StreamApp[IO] {

  override def stream(args: List[String], requestShutdown: IO[Unit]): fs2.Stream[IO, StreamApp.ExitCode] = {
    BlazeBuilder[IO]
      .mountService(WebsocketServer.service(new FunctionResolverService))
      .bindHttp(8081, "localhost")
      .serve
  }
}
