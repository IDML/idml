package io.idml.doc

import cats._
import cats.implicits._
import fastparse._, NoWhitespace._
import fastparse.{parse => fparse}

object Markdown {
  trait Node
  case class Text(s: String)                      extends Node
  case class Code(label: String, content: String) extends Node

  def backticks[_: P] = P("```")
  def codeBlock[_: P]: P[Code] = (backticks ~ CharsWhile(_ != '\n').! ~ (!backticks ~ AnyChar).rep(1).! ~ backticks).map {
    case (l, c) => Code(l, c)
  }
  def text[_: P]: P[Text]           = (!backticks ~ AnyChar).rep(1).!.map(Text.apply)
  def node[_: P]: P[Node]           = codeBlock | text
  def document[_: P]: P[List[Node]] = node.rep.map(_.toList)

  def parse(s: String) = fparse(s, document(_))
  def render(ns: List[Node]): String =
    ns.map {
      case Text(t) => t
      case Code(label, content) =>
        s"""```$label
        |${content.stripPrefix("\n").stripSuffix("\n")}
        |```""".stripMargin
    }.mkString
}
