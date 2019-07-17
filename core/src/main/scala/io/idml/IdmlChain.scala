package io.idml

import scala.collection.JavaConverters._

class IdmlChain(val engine: Idml, val transforms: Mapping*) extends Mapping {
  require(transforms != Nil, "Expected at least one transform")

  /** The first transform in the chain. This is the only one that will access input data */
  protected val head = transforms.head

  /** The remaining transforms in the chain */
  protected val tail = transforms.tail

  override def run(ctx: IdmlContext): IdmlContext = {
    ctx.listeners = engine.listeners.asScala.toList

    ctx.enterChain()
    head.run(ctx)

    ctx.input = ctx.output
    ctx.scope = ctx.output
    ctx.cursor = ctx.output
    tail.foreach(_.run(ctx))

    ctx.exitChain()

    ctx
  }

}
