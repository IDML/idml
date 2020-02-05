package io.idmlrepl

import java.nio.file.Paths
import java.util

import cats.data.EitherT
import cats.implicits._
import fansi.Color.LightRed
import io.idml.circe.IdmlCirce
import io.idml.hashing.HashingFunctionResolver
import io.idml.jsoup.JsoupFunctionResolver
import io.idml.utils.{AnalysisModule, AutoComplete}
import io.idml.{Idml, IdmlMapping, IdmlObject, Mapping, StaticFunctionResolverService}
import org.jline.reader.impl.history.DefaultHistory
import org.jline.reader._
import org.jline.terminal.TerminalBuilder
import cats.effect._
import cats.effect.concurrent.Ref

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Try
import scala.util.control.NoStackTrace

// Used to flag up commands
case class CommandException(c: String) extends Throwable with NoStackTrace

class JLineImpl[F[_]: Effect](data: Ref[F, IdmlObject]) extends JLine[F] {

  def passthroughParsedLine(s: String, c: Int): ParsedLine = new ParsedLine {
    override def word(): String             = ""
    override def wordCursor(): Int          = -1
    override def wordIndex(): Int           = -1
    override def words(): util.List[String] = new util.ArrayList()
    override def line(): String             = s
    override def cursor(): Int              = c
  }

  private val t  = TerminalBuilder.builder().system(true).build()
  private val h  = new DefaultHistory()
  private val h2 = new DefaultHistory()
  private val functions =
    (StaticFunctionResolverService.defaults(IdmlCirce).asScala ++ List(new JsoupFunctionResolver, new HashingFunctionResolver)).toList
      .flatMap { f =>
        f.providedFunctions().filterNot(_.name.startsWith("$"))
      }
  private val idml =
    Idml
      .staticBuilderWithDefaults(IdmlCirce)
      .withResolver(new JsoupFunctionResolver)
      .withResolver(new HashingFunctionResolver)
      .withResolver(new AnalysisModule)
      .build()
  private val simpleParser = new Parser {
    def parse(line: String, cursor: Int, context: Parser.ParseContext): ParsedLine =
      context match {
        case _ if line.startsWith(".") => passthroughParsedLine(line, cursor)
        case Parser.ParseContext.COMPLETE =>
          Lexer(line, cursor)
        case _ =>
          if (!line.endsWith("\n")) {
            throw new EOFError(0, 0, "You must press enter twice to finish parsing")
          } else {
            Lexer(line, cursor)
          }
      }
  }
  private val jsonParser = new Parser {
    override def parse(line: String, cursor: Int, context: Parser.ParseContext): ParsedLine = {
      if (line.startsWith(".")) passthroughParsedLine(line, cursor)
      else
        IdmlCirce
          .parseObjectEither(line)
          .map { o =>
            passthroughParsedLine(line, cursor)
          }
          .leftMap { e =>
            // if we had a parse error, ask for more input
            new EOFError(-1, -1, e.getMessage)
          }
          .toTry
          .get
    }
  }
  private val c = new Completer {
    def complete(reader: LineReader, line: ParsedLine, candidates: util.List[Candidate]): Unit = {
      // strip anything trailing
      val prefix = if (line.word() == ".") "." else ""
      val (partialLine, partialCursor) = if (line.word() == ".") {
        (line.line(), line.cursor() + 1)
      } else {
        (line.line().replace(line.word(), ""), line.cursor() + 1 - line.wordCursor())
      }
      val d           = Effect[F].toIO(data.get).unsafeRunSync()
      val suggestions = Try { AutoComplete.complete(idml)(d, partialLine, partialCursor) }.getOrElse(Set.empty)
      suggestions.foreach { suggestion =>
        candidates.add(
          new Candidate(
            prefix + suggestion,
            suggestion,
            "data",
            s"the $suggestion data node, as found in the input",
            null,
            null,
            true
          ))
      }
      // and add the functions
      functions.sortBy(_.name) foreach { f =>
        val placeholders = f.arguments.map(_._1).mkString("(", ",", ")")
        candidates.add(
          new Candidate(
            prefix + f.name + placeholders,
            f.name + placeholders,
            "function",
            f.description,
            null,
            null,
            true
          ))
      }
    }
  }
  private val lr = LineReaderBuilder
    .builder()
    .terminal(t)
    .variable(LineReader.HISTORY_FILE, Paths.get(System.getProperty("user.home"), ".idml_history"))
    .variable(LineReader.SECONDARY_PROMPT_PATTERN, ".. ")
    .history(h)
    .appName("idml repl")
    .completer(c)
    .parser(simpleParser)
    .build()
  private val jsonLr = LineReaderBuilder
    .builder()
    .terminal(t)
    .variable(LineReader.HISTORY_FILE, Paths.get(System.getProperty("user.home"), ".idml_history_json"))
    .variable(LineReader.SECONDARY_PROMPT_PATTERN, "..... ")
    .history(h2)
    .parser(jsonParser)
    .appName("idml repl")
    .build()

  override def printAbove(s: String): F[Unit] = Sync[F].delay {
    lr.printAbove(s)
  }

  def read[T](r: F[Either[Throwable, String]])(parser: String => Either[Throwable, T]): F[ReadLine[T]] =
    EitherT(r)
      .subflatMap { s =>
        Either.cond(!s.startsWith("."), s, CommandException(s))
      }
      .subflatMap(parser)
      .value
      .map {
        case Left(_: EndOfFileException | _: UserInterruptException) => EOF()
        case Left(CommandException(c))                               => Command(c)
        case Left(e)                                                 => Error(e)
        case Right(o)                                                => Result(o)
      }

  override def readJson: F[ReadLine[IdmlObject]] =
    read(Sync[F].delay { jsonLr.readLine("json> ") }.attempt)(compileJson)

  override def readIdml: F[ReadLine[Mapping]] =
    read(Sync[F].delay { lr.readLine(LightRed("~> ").render) }.attempt)(compileIdml)

  override val compileJson: String => Either[Throwable, IdmlObject] = IdmlCirce.parseObjectEither(_).leftWiden[Throwable]

  override val compileIdml: String => Either[Throwable, Mapping] = (s: String) => Try { idml.compile(s) }.toEither

  override def close: F[Unit] =
    Sync[F].delay { t.close() }
}
