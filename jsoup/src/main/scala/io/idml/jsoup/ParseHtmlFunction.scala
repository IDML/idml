package io.idml.jsoup

import io.idml.ast.{IdmlFunction, Pipeline}
import io.idml.datanodes.IString
import io.idml.{IdmlContext, InvalidCaller}

object ParseHtmlFunction extends IdmlFunction {
  override def name: String = "parseHtml"

  override def invoke(ctx: IdmlContext): Unit = {
    ctx.cursor = ctx.cursor match {
      case IString(str) =>
        IdmlJsoup.parseHtml(str)
      case _            =>
        InvalidCaller
    }
  }

  override def args: List[Pipeline] = Nil
}
