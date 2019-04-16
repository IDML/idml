package io.idml.jsoup

import io.idml.datanodes.PString
import io.idml.{InvalidCaller, PtolemyContext}
import io.idml.ast.{Pipeline, PtolemyFunction}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Whitelist

object StripTagsFunction extends PtolemyFunction {
  final val settings = new Document.OutputSettings().prettyPrint(false)

  override def name: String = "stripTags"

  override def invoke(ctx: PtolemyContext): Unit = {
    ctx.cursor = ctx.cursor match {
      case PString(str) =>
        PString(Jsoup.clean(str, "", Whitelist.none(), settings))
      case _ =>
        InvalidCaller
    }
  }

  override def args: List[Pipeline] = Nil
}
