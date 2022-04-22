package io.idml.utils
import io.idml._
import io.idml.ast._
import io.idml.datanodes.IObject

import scala.collection.mutable
import scala.collection.JavaConverters._

object Tracer {
  class Annotator(json: IdmlJson) extends IdmlListener {
    val results = mutable.ListBuffer[(Position, IdmlValue)]()

    override def exitAssignment(ctx: IdmlContext, assignment: Assignment): Unit  = {
      assignment.positions.map(p => (p.end, ctx.cursor)).foreach(results.append(_))
    }
    override def enterAssignment(ctx: IdmlContext, assignment: Assignment): Unit = ()
    override def enterChain(ctx: IdmlContext): Unit                              = ()
    override def exitChain(ctx: IdmlContext): Unit                               = ()
    override def enterPath(context: IdmlContext, path: Field): Unit              = ()
    override def exitPath(context: IdmlContext, path: Field): Unit               = ()
    override def enterPipl(context: IdmlContext, pipl: Pipeline): Unit           = ()
    override def exitPipl(context: IdmlContext, pipl: Pipeline): Unit            = ()
    override def enterFunc(ctx: IdmlContext, func: IdmlFunction): Unit           = ()
    override def exitFunc(ctx: IdmlContext, func: IdmlFunction): Unit            = ()
    override def enterMaths(context: IdmlContext, maths: Maths): Unit            = ()
    override def exitMaths(context: IdmlContext, maths: Maths): Unit             = ()

    def clear() = results.clear()

    def render(s: String): String = {
      val output = s.lines.toArray
      results.foreach { case (Position(line, char), r) =>
        output(line - 1) = output(line - 1) + " # " + json.compact(r)
      }
      output.mkString("\n")
    }
  }

  def annotate(json: IdmlJson)(s: String)(j: String): String = {
    val a   = new Annotator(json)
    val p   = Idml.autoBuilder().withListener(a).build()
    val ctx = new IdmlContext(json.parse(j), IObject(), List[IdmlListener](a))
    p.compile(s).run(ctx).output
    a.render(s)
  }

}
