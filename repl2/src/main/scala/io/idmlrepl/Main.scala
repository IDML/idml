package io.idmlrepl

import java.io.EOFException
import java.nio.file.{Path, Paths}
import java.util
import java.util.regex.Pattern

import cats.implicits._
import io.idml.{Idml, IdmlMapping, StaticFunctionResolverService}
import io.idml.ast.AstGenerator
import io.idml.circe.IdmlCirce
import io.idml.hashing.HashingFunctionResolver
import io.idml.jsoup.JsoupFunctionResolver
import io.idml.lang.{MappingLexer, MappingParser, ThrowConsoleErrorListener}
import io.idml.utils.{AnalysisModule, AutoComplete}
import org.antlr.v4.runtime.{ANTLRInputStream, CommonTokenStream}
import org.jline.reader.impl.DefaultHighlighter
import org.jline.reader.{Candidate, Completer, CompletingParsedLine, EOFError, Expander, Highlighter, History, LineReader, LineReaderBuilder, ParsedLine, Parser, SyntaxError}
import org.jline.reader.impl.history.DefaultHistory
import org.jline.terminal.TerminalBuilder
import org.jline.utils.AttributedString

import scala.collection.JavaConverters._
import scala.util.Try
import fansi.Color.LightRed

object Main {

  def main(args: Array[String]): Unit = {
    val t = TerminalBuilder.builder().system(true).build()
    val h = new DefaultHistory()
    val h2 = new DefaultHistory()
    val functions =
      (StaticFunctionResolverService.defaults(IdmlCirce).asScala ++ List(new JsoupFunctionResolver, new HashingFunctionResolver)).toList
        .flatMap { f =>
          f.providedFunctions().filterNot(_.name.startsWith("$"))
        }
    val idml =
      Idml
        .staticBuilderWithDefaults(IdmlCirce)
        .withResolver(new JsoupFunctionResolver)
        .withResolver(new HashingFunctionResolver)
        .withResolver(new AnalysisModule)
        .build()
    val simpleParser = new Parser {
      def parse(line: String, cursor: Int, context: Parser.ParseContext): ParsedLine =
        context match {
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
    val jsonParser = new Parser {
      override def parse(line: String, cursor: Int, context: Parser.ParseContext): ParsedLine = {
        IdmlCirce.parseObjectEither(line).map { o =>
          new ParsedLine {
            override def word(): String = ""
            override def wordCursor(): Int = -1
            override def wordIndex(): Int = -1
            override def words(): util.List[String] = new util.ArrayList()
            override def line(): String = line
            override def cursor(): Int = cursor
          }
        }.leftMap { e =>
          new EOFError(-1, -1, e.getMessage)
        }.toTry.get
      }
    }
    var data = IdmlCirce.parseObject("""{}""")
    val c = new Completer {
      def complete(reader: LineReader, line: ParsedLine, candidates: util.List[Candidate]): Unit = {
        // strip anything trailing
        val prefix = if (line.word() == ".") "." else ""
        val (partialLine, partialCursor) = if (line.word() == ".") {
          (line.line(), line.cursor()+1)
        } else {
          (line.line().replace(line.word(), ""), line.cursor()+1-line.wordCursor())
        }
        val suggestions = AutoComplete.complete(idml)(data, partialLine, partialCursor)
        suggestions.foreach { suggestion =>
          candidates.add(new Candidate(
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
        functions.sortBy(_.name)foreach { f =>
          val placeholders = f.arguments.map(_._1).mkString("(", ",", ")")
          candidates.add(new Candidate(
            prefix + f.name+placeholders,
            f.name+placeholders,
            "function",
            f.description,
            null,
            null,
            true
          ))
        }
      }
    }
    val lr = LineReaderBuilder.builder()
      .terminal(t)
      .variable(LineReader.HISTORY_FILE, Paths.get(System.getProperty("user.home"), ".idml_history"))
      .variable(LineReader.SECONDARY_PROMPT_PATTERN, ".. ")
      .history(h)
      .appName("idml repl")
      .completer(c)
      .parser(simpleParser)
      .build()
    val jsonLr = LineReaderBuilder.builder()
      .terminal(t)
      .variable(LineReader.HISTORY_FILE, Paths.get(System.getProperty("user.home"), ".idml_history_json"))
      .variable(LineReader.SECONDARY_PROMPT_PATTERN, "..... ")
      .history(h2)
      .parser(jsonParser)
      .appName("idml repl")
      .build()
    data = IdmlCirce.parseObject(jsonLr.readLine("json> "))
    while (true) {
      val input = lr.readLine(LightRed("~> ").render)
      lr.printAbove(IdmlCirce.pretty(idml.compile(input).run(data)))
    }
  }


}
