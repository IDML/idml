package io.idml.tool

import java.io.File
import java.net.URI

import cats._
import cats.effect._
import cats.implicits._
import cats.syntax._
import cats.data._
import com.monovore.decline._
import DeclineHelpers._
import io.idml.BuildInfo
import io.idml.tool.IOCommandApp

object IdmlTool extends IOCommandApp[IO[ExitCode]] {
  override def name: String                    = "idml"
  override def header: String                  = "IDML command line tools"
  override def version: String                 = BuildInfo.version
  override def commandLine: Opts[IO[ExitCode]] =
    NonEmptyList
      .of(
        io.idmlrepl.Main.execute(),
        IdmlTools.apply,
        IdmlTools.server,
        io.idml.test.Main.execute(),
        io.idml.tutor.Main.execute(),
        SanityCheck.run
      )
      .map(c => Opts.subcommand(c))
      .reduceK

  override def main(c: IO[ExitCode]): IO[ExitCode] = c
}
