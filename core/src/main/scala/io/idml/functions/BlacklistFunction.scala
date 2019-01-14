package io.idml.functions

import io.idml.ast.{Pipeline, PtolemyFunction}
import io.idml.{InvalidParameters, PtolemyContext, PtolemyNothing, PtolemyObject}

case class BlacklistFunction(args: List[Pipeline]) extends PtolemyFunction {

  /** Strip outs blacklisted fields */
  override def invoke(ctx: PtolemyContext): Unit = {
    ctx.cursor = ctx.cursor match {
      case nothing: PtolemyNothing =>
        nothing
      case obj: PtolemyObject =>
        val keys = args.flatMap(_.eval(ctx).toStringOption)
        if (keys.size != args.size) {
          InvalidParameters
        } else {
          val blacklisted = obj.deepCopy.asInstanceOf[PtolemyObject]
          keys.foreach(blacklisted.fields.remove)
          blacklisted
        }
    }
  }

  override def name: String = "blacklist"
}
