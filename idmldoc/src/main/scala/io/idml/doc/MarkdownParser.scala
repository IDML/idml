package io.idml.doc

import cats._, cats.implicits._
import fastparse.all._

object MarkdownParser {

  trait Node
  case class Text(s: String) extends Node
  case class Code(label: String, content: String) extends Node

  val backticks = P("```")

  val codeBlock: Parser[Code] = (backticks ~ CharsWhile(_ != '\n').! ~ (!backticks ~ AnyChar).rep(min = 1).! ~ backticks).map{ case (l, c) => Code(l, c)}

  val text: Parser[Text] = (!backticks ~ AnyChar).rep(min  = 1).!.map(Text.apply)

  val node: Parser[Node] = codeBlock | text

  val document: Parser[List[Node]] = node.rep.map(_.toList)

}
