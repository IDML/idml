package io.idml.lang

import org.antlr.v4.runtime.{ConsoleErrorListener, RecognitionException, Recognizer}

/** Thrown when IDML failed to parse */
// scalastyle:off null
class DocumentParseException(str: String, ex: Exception = null) extends Exception(str, ex)
// scalastyle:on null

/** Causes an antlr parser or lexer to throw an exception when an error is encountered */
class ThrowConsoleErrorListener extends ConsoleErrorListener {
  override def syntaxError(
      recognizer: Recognizer[_, _],
      offendingSymbol: AnyRef,
      line: Int,
      charPositionInLine: Int,
      msg: String,
      ex: RecognitionException
  ) {
    throw new DocumentParseException("Line " + line + ":" + charPositionInLine + " " + msg, ex)
  }
}
