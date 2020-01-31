package io.idmlrepl

import java.util

import io.idml.lang.MappingLexer
import org.antlr.v4.runtime.{ANTLRInputStream, CommonTokenStream}
import org.jline.reader.{CompletingParsedLine, ParsedLine}
import scala.collection.JavaConverters._

object Lexer {
  def apply(s: String, pointer: Int): ParsedLine = {
    val c = pointer-1
    val lexer  = new MappingLexer(new ANTLRInputStream(s))
    val stream = new CommonTokenStream(lexer)
    stream.fill()
    val tokens = stream.getTokens.asScala.toList
    lexer.removeErrorListeners()
    val currentToken = tokens.find { t =>
      t.getStartIndex <= c && c <= t.getStopIndex
    }.orElse {
      // we're between tokens so pick the last one
      tokens.filter(_.getStartIndex > c).lastOption
    }.orElse(tokens.lastOption)
    val currentIndex = currentToken.map(tokens.indexOf).getOrElse(0)
    new CompletingParsedLine {
      def word(): String = currentToken.map(_.getText).getOrElse("")
      def wordCursor(): Int = currentToken.map(c - _.getStartIndex).getOrElse(0)
      def wordIndex(): Int = currentIndex
      def words(): util.List[String] = tokens.map(_.getText).asJava
      def line(): String = s
      def cursor(): Int = c
      def escape(candidate: CharSequence, complete: Boolean): CharSequence = candidate
      def rawWordCursor(): Int = wordCursor()
      def rawWordLength(): Int = word().length
    }
  }
}
