package io.idml.utils.folders

import io.idml.ast._

object Folders {
  implicit class FoldableRule(r: Rule) {
    def fold[O](
        onAssignment: Assignment => O,
        onReassignment: Reassignment => O,
        onVariable: Variable => O
    ): O = r match {
      case a: Assignment   => onAssignment(a)
      case r: Reassignment => onReassignment(r)
      case v: Variable     => onVariable(v)
    }
  }

  implicit class FoldableExpression(e: Expression) {
    def fold[O](
        onCoalesce: Coalesce => O,
        onExecNav: ExecNav => O,
        onFilter: Filter => O,
        onMatch: Match => O,
        onCase: Case => O,
        onIf: If => O,
        onField: Field => O,
        onSlice: Slice => O,
        onIndex: Index => O,
        onAstArray: AstArray => O,
        onAstObject: AstObject => O,
        onMaths: Maths => O,
        onPtolemyFunction: PtolemyFunction => O,
        onWildcard: Wildcard => O
    ): O = e match {
      case c: Coalesce        => onCoalesce(c)
      case e: ExecNav         => onExecNav(e)
      case f: Filter          => onFilter(f)
      case m: Match           => onMatch(m)
      case c: Case            => onCase(c)
      case i: If              => onIf(i)
      case f: Field           => onField(f)
      case s: Slice           => onSlice(s)
      case i: Index           => onIndex(i)
      case a: AstArray        => onAstArray(a)
      case o: AstObject       => onAstObject(o)
      case m: Maths           => onMaths(m)
      case f: PtolemyFunction => onPtolemyFunction(f)
      case w: Wildcard        => onWildcard(w)
    }
  }

  implicit class FoldableExecNav(e: ExecNav) {
    def fold[O](
        onExecNavLiteral: ExecNavLiteral => O,
        onExecNavAbsolute: => O,
        onExecNavRelative: => O,
        onExecNavTemp: => O,
        onExecNavVariable: => O
    ): O = e match {
      case l: ExecNavLiteral => onExecNavLiteral(l)
      case ExecNavAbsolute   => onExecNavAbsolute
      case ExecNavRelative   => onExecNavRelative
      case ExecNavTemp       => onExecNavTemp
      case ExecNavVariable   => onExecNavVariable
    }
  }
}
