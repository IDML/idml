package io.idml.lang

import java.nio.charset.Charset

import com.google.common.io.Resources
import org.antlr.v4.runtime._
import org.scalatest.FunSuite
import org.scalatest.MustMatchers

/**
  * Created by jon on 11/04/2014.
  */
class MappingTestBase extends FunSuite with MustMatchers {

  def test(filename: String) {
    super.test("Parsing " + filename) {
      val str    = Resources.toString(Resources.getResource(filename), Charset.defaultCharset())
      val input  = new ANTLRInputStream(str)
      val lexer  = new MappingLexer(input)
      val tokens = new CommonTokenStream(lexer)
      val parser = new MappingParser(tokens)

      lexer.removeErrorListeners()
      parser.removeErrorListeners()
      //parser.addErrorListener(new DiagnosticErrorListener)
      parser.addErrorListener(new ThrowConsoleErrorListener)

      parser.document()
    }
  }
}
