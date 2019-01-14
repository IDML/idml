package io.idml

import io.idml.datanodes.PObject
import io.idml.ast.Document
import scala.collection.JavaConverters._

abstract class Mapping {
  def run(ctx: PtolemyContext): PtolemyContext
  def run(input: PtolemyValue): PtolemyObject                        = run(new PtolemyContext(input, PObject())).output
  def run(input: PtolemyValue, output: PtolemyObject): PtolemyObject = run(new PtolemyContext(input, output)).output
}

object Mapping {
  import cats._, cats.data._, cats.implicits._

  def fromMultipleMappings(engine: Ptolemy, ms: List[Mapping]): Mapping =
    fromMultipleMappings(engine, ms.asJava)

  def fromMultipleMappings(engine: Ptolemy, ms: java.util.List[Mapping]): Mapping =
    (ctx: PtolemyContext) => {
      val result = ms.asScala.toList.map { m =>
        ctx.output = PObject()
        m.run(ctx)
        ctx.output.asInstanceOf[PObject]
      }
      ctx.output = NonEmptyList.fromList(result).map(_.reduceLeft(_ deepMerge _)).getOrElse(PObject())
      ctx
    }
}

class PtolemyMapping(val nodes: Document) extends Mapping {
  override def run(ctx: PtolemyContext): PtolemyContext = {
    ctx.doc = nodes
    nodes.invoke(ctx)
    ctx
  }
}
