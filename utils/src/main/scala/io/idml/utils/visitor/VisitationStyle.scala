package io.idml.utils.visitor

/** The visitation style determines how we traverse the document */
trait VisitationStyle {
  this: ExecNodeVisitor =>

  def visitDoc(ctx: ExecDocContext)
  def visitBlockInvoke(ctx: ExecBlockInvokeContext)
}

/** Visit things in the order that they are called, discovering all the paths the interpreter could take */
trait ExecutionOrderVisitationStyle extends VisitationStyle {
  this: ExecNodeVisitor =>

  /** Visit the main block and expect apply() to call the others */
  def visitDoc(ctx: ExecDocContext) {
    visitBlock(ExecBlockContext(ctx.doc.main, ctx))
  }

  /** Visit an apply method, potentially calling */
  def visitBlockInvoke(ctx: ExecBlockInvokeContext) {
    val top   = ctx.parent.parent.parent.parent
    val block = top.doc.blocks.getOrElse(ctx.expr.name, throw new IllegalArgumentException(s"Unknown block ${ctx.expr.name}"))
    visitBlock(ExecBlockContext(block, top))
  }

}

/** A visitation style that visits the main block and ignores the rest */
trait OnlyMainVisitationStyle extends VisitationStyle {
  this: ExecNodeVisitor =>

  /** Visit the main block */
  def visitDoc(ctx: ExecDocContext) {
    visitBlock(ExecBlockContext(ctx.doc.main, ctx))
  }

  /** This is a no-op function: Other blocks are ignored */
  def visitBlockInvoke(ctx: ExecBlockInvokeContext): Unit = ()
}

/** Visit all the blocks as if they were top-level. Good to check for features and things like that */
trait StructureAgnosticVisitationStyle extends VisitationStyle {
  this: ExecNodeVisitor =>

  /** Traverse all blocks in the top level */
  def visitDoc(ctx: ExecDocContext) {
    ctx.doc.blocks.values
      .map(ExecBlockContext(_, ctx))
      .foreach(visitBlock)
  }

  /** This is a no-op function: blocks are traversed at the top level */
  def visitBlockInvoke(ctx: ExecBlockInvokeContext): Unit = ()
}
