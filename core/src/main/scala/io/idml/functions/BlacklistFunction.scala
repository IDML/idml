package io.idml.functions

import io.idml.ast.{IdmlFunction, Pipeline}
import io.idml.{IdmlContext, IdmlNothing, IdmlObject, InvalidParameters}

case class BlacklistFunction(args: List[Pipeline]) extends IdmlFunction {

  /** Strip outs blacklisted fields */
  override def invoke(ctx: IdmlContext): Unit = {
    ctx.cursor = ctx.cursor match {
      case nothing: IdmlNothing =>
        nothing
      case obj: IdmlObject =>
        val keys = args.flatMap(_.eval(ctx).toStringOption)
        if (keys.size != args.size) {
          InvalidParameters
        } else {
          val blacklisted = obj.deepCopy.asInstanceOf[IdmlObject]
          keys.foreach(blacklisted.fields.remove)
          blacklisted
        }
    }
  }

  override def name: String = "blacklist"
}
