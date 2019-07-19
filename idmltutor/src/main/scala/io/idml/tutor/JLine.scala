package io.idml.tutor

import java.util

import cats.effect.Sync
import org.jline.reader._
import org.jline.terminal.{Terminal, TerminalBuilder}
import org.jline.utils.AttributedString
import org.jline.reader.impl.history.DefaultHistory

import scala.collection.JavaConverters._

class JLine[F[_]](name: String, t: Terminal)(implicit F: Sync[F]) {
  val history = new DefaultHistory()
  val reader = LineReaderBuilder
    .builder()
    .terminal(t)
    .appName(name)
    .variable(LineReader.HISTORY_FILE, "~/.idmltutor_history")
    .history(history)
    .build()
  val multiReader = LineReaderBuilder
    .builder()
    .terminal(t)
    .appName(name)
    .variable(LineReader.HISTORY_BEGINNING_SEARCH_BACKWARD, "~/.idmltutor_multi_history")
    .variable(LineReader.SECONDARY_PROMPT_PATTERN, "~> ")
    .history(history)
    .parser(new Parser {
      override def parse(currentLine: String, currentCursor: Int, context: Parser.ParseContext): ParsedLine = {
        if (!currentLine.endsWith("\n")) {
          throw new EOFError(0, currentCursor, "gotta end with double enter")
        }
        new ParsedLine {
          override def word(): String             = currentLine
          override def wordCursor(): Int          = currentCursor
          override def wordIndex(): Int           = 0
          override def words(): util.List[String] = List(currentLine).asJava
          override def line(): String             = currentLine
          override def cursor(): Int              = currentCursor
        }
      }
    })
    .build()

  def shutdown(): F[Unit] = F.delay {
    history.save()
  }

  // useful things
  def flush(): F[Unit] = F.delay { t.flush() }

  def width(): F[Int]                          = F.delay { t.getWidth }
  def readLine(prompt: String): F[String]      = F.delay { reader.readLine(prompt) }
  def readMultiline(prompt: String): F[String] = F.delay { multiReader.readLine(prompt) }
  def printAbove(string: String): F[Unit]      = F.delay { reader.printAbove(string) }
}
object JLine {
  def apply[F[_]](name: String)(implicit F: Sync[F]): F[JLine[F]] = F.delay {
    new JLine[F](name,
                 TerminalBuilder
                   .builder()
                   .jansi(true)
                   .build())
  }
}
