package io.idml.jsoup

import io.idml.datanodes.PString
import io.idml.{InvalidCaller, PtolemyContext}
import io.idml.ast.{Pipeline, PtolemyFunction}
import org.jsoup.Jsoup

object StripTagsFunction extends PtolemyFunction {

  override def name: String = "stripTags"

  override def invoke(ctx: PtolemyContext): Unit = {
    ctx.cursor = ctx.cursor match {
      case PString(str) =>
        PString(Jsoup.parse(str).text())
      case _ =>
        InvalidCaller
    }
  }

  override def args: List[Pipeline] = Nil
}
