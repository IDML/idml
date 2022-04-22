package io.idml.jsoup

import io.idml.ast.{IdmlFunction, Pipeline}
import io.idml.datanodes.IString
import io.idml.{IdmlContext, InvalidCaller}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import org.jsoup.safety.Whitelist

object ParseXmlFunction extends IdmlFunction {
  override def name: String = "parseXml"

  override def invoke(ctx: IdmlContext): Unit = {
    ctx.cursor = ctx.cursor match {
      case IString(str) =>
        IdmlJsoup.parseXml(str)
      case _            =>
        InvalidCaller
    }
  }

  override def args: List[Pipeline] = Nil
}
