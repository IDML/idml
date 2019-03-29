import java.io.File
import java.net.URI

import cats._
import cats.effect._
import cats.implicits._
import cats.syntax._
import cats.data._
import com.monovore.decline._
import DeclineHelpers._
import io.idml.{Ptolemy, PtolemyConf, PtolemyJson, UnmappedFieldsFinder}
import io.idml.utils.DocumentValidator
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

object IdmlTool
    extends CommandApp(
      name = "idml",
      header = "IDML command line tools",
      main = {
        NonEmptyList
          .of(
            IdmlTools.repl,
            IdmlTools.apply,
            IdmlTools.server,
            io.idml.test.Main.execute().map { f =>
              f.unsafeRunSync()
              ()
            }
          )
          .map(c => Opts.subcommand(c))
          .reduceK
      }
    ) {}
