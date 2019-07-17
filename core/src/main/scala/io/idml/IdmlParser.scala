package io.idml

import java.io.{InputStream, Reader}

import io.idml.lang.{MappingLexer, MappingParser, ThrowConsoleErrorListener}
import io.idml.ast.AstGenerator
import org.antlr.v4.runtime.{ANTLRInputStream, CommonTokenStream}

/**
  * Parses Idml documents
  */
class IdmlParser {

  /** Create an execution tree from a string */
  def parse(engine: Idml, in: String): IdmlMapping = {
    parse(engine, new ANTLRInputStream(in))
  }

  /** Create an execution tree from a reader */
  def parse(engine: Idml, in: Reader): IdmlMapping = {
    parse(engine, new ANTLRInputStream(in))
  }

  /** Create an execution tree from an input stream */
  def parse(engine: Idml, in: InputStream): IdmlMapping = {
    parse(engine, new ANTLRInputStream(in))
  }

  /** Create an execution tree from any ANTLR input stream */
  def parse(engine: Idml, in: ANTLRInputStream): IdmlMapping = {
    val lexer  = new MappingLexer(in)
    val tokens = new CommonTokenStream(lexer)
    val parser = new MappingParser(tokens)
    lexer.removeErrorListeners()
    parser.removeErrorListeners()
    parser.addErrorListener(new ThrowConsoleErrorListener)
    parser.setBuildParseTree(true)
    val inner =
      new AstGenerator(engine.functionResolver).visitDocument(parser.document())
    new IdmlMapping(inner)
  }
}
