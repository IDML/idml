package io.idml

import io.idml.datanodes.IObject
import io.idml.ast.Document
import scala.collection.JavaConverters._

abstract class Mapping {
  def run(ctx: IdmlContext): IdmlContext
  def run(input: IdmlValue): IdmlObject                     = run(new IdmlContext(input, IObject())).output
  def run(input: IdmlValue, output: IdmlObject): IdmlObject = run(new IdmlContext(input, output)).output
}

object Mapping {
  import cats._, cats.data._, cats.implicits._

  def fromMultipleMappings(ms: List[Mapping]): Mapping =
    fromMultipleMappings(ms.asJava)

  def fromMultipleMappings(ms: java.util.List[Mapping]): Mapping =
    (ctx: IdmlContext) => {
      val result = ms.asScala.toList.map { m =>
        ctx.output = IObject()
        m.run(ctx)
        ctx.output.asInstanceOf[IObject]
      }
      ctx.output = NonEmptyList.fromList(result).map(_.reduceLeft(_ deepMerge _)).getOrElse(IObject())
      ctx
    }
}

class IdmlMapping(val nodes: Document) extends Mapping {
  override def run(ctx: IdmlContext): IdmlContext = {
    ctx.doc = nodes
    nodes.invoke(ctx)
    ctx
  }
}
