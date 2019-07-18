package io.idml

import java.io.{InputStream, Reader}

import io.idml.lang.{DocumentParseException, MappingLexer, MappingParser, ThrowConsoleErrorListener}
import io.idml.ast.AstGenerator
import org.antlr.v4.runtime.{ANTLRInputStream, CommonTokenStream}

/**
  * Parses Idml documents
  */
class IdmlParser {

  /** Create an execution tree from a string */
  @throws[DocumentParseException]("if it fails to parse the document")
  def parse(funcs: FunctionResolverService, in: String): IdmlMapping = {
    parse(funcs, new ANTLRInputStream(in))
  }

  /** Create an execution tree from a reader */
  @throws[DocumentParseException]("if it fails to parse the document")
  def parse(funcs: FunctionResolverService, in: Reader): IdmlMapping = {
    parse(funcs, new ANTLRInputStream(in))
  }

  /** Create an execution tree from an input stream */
  @throws[DocumentParseException]("if it fails to parse the document")
  def parse(funcs: FunctionResolverService, in: InputStream): IdmlMapping = {
    parse(funcs, new ANTLRInputStream(in))
  }

  /** Create an execution tree from any ANTLR input stream */
  @throws[DocumentParseException]("if it fails to parse the document")
  def parse(funcs: FunctionResolverService, in: ANTLRInputStream): IdmlMapping = {
    val lexer  = new MappingLexer(in)
    val tokens = new CommonTokenStream(lexer)
    val parser = new MappingParser(tokens)
    lexer.removeErrorListeners()
    parser.removeErrorListeners()
    parser.addErrorListener(new ThrowConsoleErrorListener)
    parser.setBuildParseTree(true)
    val inner =
      new AstGenerator(funcs).visitDocument(parser.document())
    new IdmlMapping(inner)
  }
}
