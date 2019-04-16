package io.idml.jsoup

import io.idml.datanodes.PString
import io.idml.{InvalidCaller, PtolemyContext}
import io.idml.ast.{Pipeline, PtolemyFunction}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import org.jsoup.safety.Whitelist

object StripTagsFunction extends PtolemyFunction {
  override def name: String = "stripTags"

  override def invoke(ctx: PtolemyContext): Unit = {
    ctx.cursor = ctx.cursor match {
      case PString(str) =>
        PString(
          Parser.unescapeEntities(
            Jsoup.clean(str, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false)),
            false
          )
        )
      case _ =>
        InvalidCaller
    }
  }

  override def args: List[Pipeline] = Nil
}
