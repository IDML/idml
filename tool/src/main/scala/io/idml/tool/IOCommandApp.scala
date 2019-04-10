package io.idml.tool

import cats.effect.{ExitCode, IO, IOApp}
import com.monovore.decline.{Command, Opts, PlatformApp, Visibility}

trait IOCommandApp[T] extends IOApp {
  def name: String
  def header: String
  def commandLine: Opts[T]
  def main(t: T): IO[ExitCode]
  def helpFlag: Boolean = true
  def version: String   = ""

  def command: Command[IO[ExitCode]] = {
    val showVersion =
      if (version.isEmpty) Opts.never
      else
        Opts
          .flag("version", "Print the version number and exit.", visibility = Visibility.Partial)
          .map { _ =>
            IO { System.err.println(version); ExitCode.Success }
          }
    Command(name, header, helpFlag)(showVersion orElse commandLine.map(main))
  }

  override def run(args: List[String]): IO[ExitCode] = command.parse(PlatformApp.ambientArgs getOrElse args, sys.env) match {
    case Left(help) => IO { System.err.println(help); ExitCode.Error }
    case Right(r)   => r
  }
}
