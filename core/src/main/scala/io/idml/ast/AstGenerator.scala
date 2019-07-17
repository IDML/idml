// scalastyle:off null
// scalastyle:off underscore.import
// scalastyle:off number.of.methods
package io.idml.ast

import atto.Parser
import io.idml.datanodes._
import io.idml.lang.MappingParser._
import io.idml.lang.MappingVisitor
import io.idml.FunctionResolverService
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor

import scala.collection.JavaConverters._

/** Traverses the mapping language syntax tree to create the Node tree */
class AstGenerator(functionResolver: FunctionResolverService) extends AbstractParseTreeVisitor[Node] with MappingVisitor[Node] {

  /** Create a part pipeline stage */
  override def visitPart(ctx: PartContext): Expression = {
    if (ctx.function() != null) {
      visitFunction(ctx.function())
    } else if (ctx.label() != null) {
      visitLabel(ctx.label())
    } else if (ctx.coalesce() != null) {
      visitCoalesce(ctx.coalesce())
    } else if (ctx.Any() != null) {
      Any
    } else if (ctx.Wildcard() != null) {
      Wildcard(Pipeline(Nil))
    } else {
      throw new IllegalStateException()
    }
  }

  /** Create a filter pipeline stage */
  override def visitFilter(ctx: FilterContext): Filter = {
    Filter(visitPredicate(ctx.predicate()))
  }

  /** Create a slice pipeline stage */
  override def visitSlice(ctx: SliceContext): Expression = {
    val left  = Option(ctx.sliceLeft().Int()).map(_.getText.toInt)
    val right = Option(ctx.sliceRight().Int()).map(_.getText.toInt)
    Slice(left, right)
  }

  /** Creates a modifier pipeline stage */
  override def visitModifier(ctx: ModifierContext): Expression = {
    if (ctx.filter() != null) {
      visitFilter(ctx.filter())
    } else if (ctx.index() != null) {
      visitIndex(ctx.index())
    } else if (ctx.slice() != null) {
      visitSlice(ctx.slice())
    } else {
      throw new IllegalStateException()
    }
  }

  /** Creates a label */
  override def visitLabel(ctx: LabelContext): Field = {
    if (ctx.BacktickLabel() != null) {
      Field(ctx.BacktickLabel().getText.replace("`", ""))
    } else if (ctx.PlainLabel() != null) {
      Field(ctx.PlainLabel().getText)
    } else {
      throw new IllegalStateException()
    }
  }

  /** Creates an index pipeline stage */
  override def visitIndex(ctx: IndexContext): Index = {
    // Negate the index if we had a minus sign in front
    ctx.Minus() match {
      case null => Index(ctx.Int().getText.toInt)
      case _    => Index(0 - ctx.Int().getText.toInt)
    }
  }

  /** Creates a coalesce stage */
  override def visitCoalesce(ctx: CoalesceContext): Coalesce = {
    val pipelines = ctx.pipeline().asScala.map(visitPipeline).toList
    Coalesce(pipelines)
  }

  /** Creates a function pipeline stage */
  override def visitFunction(ctx: FunctionContext): Expression = {
    val name = ctx.label().getText
    val args = Option(ctx.arguments()) match {
      case Some(a) => a.argument().asScala.map(visitArgument).toList
      case None    => Nil
    }

    functionResolver.resolve(name, args)
  }

  override def visitArgument(ctx: ArgumentContext): Argument =
    ctx match {
      case c if c.pipeline() != null  => visitPipeline(ctx.pipeline())
      case c if c.predicate() != null => visitPredicate(ctx.predicate())
    }

  /** Invalid in this implementation */
  override def visitArguments(ctx: ArgumentsContext): Node =
    throw new IllegalStateException()

  /** Invalid in this implementation */
  override def visitDestination(ctx: DestinationContext): Node =
    throw new IllegalStateException()

  /** Invalid in this implementation */
  override def visitHeader(ctx: HeaderContext): Node =
    throw new IllegalStateException()

  /** Invalid in this implementation */
  override def visitSliceLeft(ctx: SliceLeftContext): Node =
    throw new IllegalStateException()

  /** Invalid in this implementation */
  override def visitSliceRight(ctx: SliceRightContext): Node =
    throw new IllegalStateException()

  /** Invalid in this implementation */
  override def visitLiteralExp(ctx: LiteralExpContext): Node =
    throw new IllegalStateException()

  /** Invalid in this implementation */
  override def visitThisRelativePathExp(ctx: ThisRelativePathExpContext): Node =
    throw new IllegalStateException()

  /** Invalid in this implementation */
  override def visitAbsolutePathExp(ctx: AbsolutePathExpContext): Node =
    throw new IllegalStateException()

  /** Invalid in this implementation */
  override def visitVariableExp(ctx: VariableExpContext): Node =
    throw new IllegalStateException()

  /** Invalid in this implementation */
  override def visitRelativePathExp(ctx: RelativePathExpContext): Node =
    throw new IllegalStateException()

  /** Invalid in this implementation */
  override def visitTempVariableExp(ctx: TempVariableExpContext): Node =
    throw new IllegalStateException()

  override def visitArrayPathExp(ctx: ArrayPathExpContext): Node =
    throw new IllegalStateException()

  override def visitEmptyArray(ctx: EmptyArrayContext): Node =
    throw new IllegalStateException()

  override def visitArrayWithStuffIn(ctx: ArrayWithStuffInContext): Node =
    throw new IllegalStateException()

  override def visitObjectPathExp(ctx: ObjectPathExpContext): Node =
    throw new IllegalStateException()

  override def visitEmptyObject(ctx: EmptyObjectContext): Node =
    throw new IllegalStateException()

  override def visitObjectWithStuffIn(ctx: ObjectWithStuffInContext): Node =
    throw new IllegalStateException()

  /** Continues the creation of a pipeline */
  override def visitExpressionChain(ctx: ExpressionChainContext): Pipeline = {
    val filter    = Option(ctx.filter()).map(visitFilter).toList
    val expr      = visitPart(ctx.part())
    val modifiers = ctx.modifier().asScala.map(visitModifier).toList
    val tail      = ctx.expressionChain().asScala.map(visitExpressionChain).toList
    filter :: expr :: modifiers :: tail :: Pipeline(Nil)
  }

  /** Signifies that the pipeline is a literal, like a string or int  */
  override def visitLiteralExpression(ctx: LiteralExpressionContext): Pipeline = {
    val literal   = visitLiteral(ctx.literal())
    val filter    = Option(ctx.filter()).map(visitFilter).toList
    val expr      = ExecNavLiteral(literal)
    val modifiers = ctx.modifier().asScala.map(visitModifier).toList
    val chain     = Option(ctx.expressionChain()).map(visitExpressionChain).toList
    filter :: expr :: modifiers :: chain :: Pipeline(Nil)
  }

  /** Signifies that the pipeline begins as an absolute path into the input document */
  override def visitAbsolutePathExpression(ctx: AbsolutePathExpressionContext): Pipeline = {
    val filter    = Option(ctx.filter()).map(visitFilter).toList
    val expr      = ExecNavAbsolute
    val modifiers = ctx.modifier().asScala.map(visitModifier).toList
    val chain     = Option(ctx.expressionChain()).map(visitExpressionChain).toList
    filter :: expr :: modifiers :: chain :: Pipeline(Nil)
  }

  /** Signifies that the pipeline begins with a variable */
  override def visitVariableExpression(ctx: VariableExpressionContext): Pipeline = {
    val filter    = Option(ctx.filter()).map(visitFilter).toList
    val expr      = ExecNavVariable
    val part      = visitPart(ctx.part())
    val modifiers = ctx.modifier().asScala.map(visitModifier).toList
    val chain     = Option(ctx.expressionChain()).map(visitExpressionChain).toList
    filter :: expr :: part :: modifiers :: chain :: Pipeline(Nil)
  }

  /** Signifies that the pipeline begins with a variable */
  override def visitTempVariableExpression(ctx: TempVariableExpressionContext): Pipeline = {
    val filter    = Option(ctx.filter()).map(visitFilter).toList
    val expr      = ExecNavTemp
    val part      = visitPart(ctx.part())
    val modifiers = ctx.modifier().asScala.map(visitModifier).toList
    val chain     = Option(ctx.expressionChain()).map(visitExpressionChain).toList
    filter :: expr :: part :: modifiers :: chain :: Pipeline(Nil)
  }

  /** Signifies that the pipeline begins with a relative path like 'a.b.c' */
  override def visitRelativePathExpression(ctx: RelativePathExpressionContext): Pipeline = {
    val expr  = ExecNavRelative
    val chain = Option(ctx.expressionChain()).map(visitExpressionChain).toList
    expr :: chain :: Pipeline(Nil)
  }

  /** Signifies that the pipeline begins with a relative path with the 'this' keyword,
    * e.g. 'this.a.b' */
  override def visitThisRelativePathExpression(ctx: ThisRelativePathExpressionContext): Pipeline = {
    val filter    = Option(ctx.filter()).map(visitFilter).toList
    val expr      = ExecNavRelative
    val modifiers = ctx.modifier().asScala.map(visitModifier).toList
    val chain     = Option(ctx.expressionChain()).map(visitExpressionChain).toList
    filter :: expr :: modifiers :: chain :: Pipeline(Nil)
  }

  override def visitArrayPathExpression(ctx: ArrayPathExpressionContext): Pipeline = {
    val filter = Option(ctx.filter()).map(visitFilter).toList
    val expr   = ExecNavTemp
    val arr = ctx.array() match {
      case e: EmptyArrayContext       => AstArray(List())
      case s: ArrayWithStuffInContext => AstArray(s.pipeline().asScala.map(visitPipeline).toList)
    }
    val modifiers = ctx.modifier().asScala.map(visitModifier).toList
    val chain     = Option(ctx.expressionChain()).map(visitExpressionChain).toList
    filter :: expr :: arr :: modifiers :: chain :: Pipeline(Nil)
  }

  override def visitObjectPathExpression(ctx: ObjectPathExpressionContext): Pipeline = {
    val filter = Option(ctx.filter()).map(visitFilter).toList
    val expr   = ExecNavTemp
    val obj = ctx.`object`() match {
      case e: EmptyObjectContext => AstObject(Map.empty)
      case s: ObjectWithStuffInContext =>
        AstObject(s.String().asScala.map(x => decodeString(x.toString())).zip(s.pipeline().asScala.map(visitPipeline)).toMap)
    }
    val modifiers = ctx.modifier().asScala.map(visitModifier).toList
    val chain     = Option(ctx.expressionChain()).map(visitExpressionChain).toList
    filter :: expr :: obj :: modifiers :: chain :: Pipeline(Nil)
  }

  /** Create a named section that contains mappings */
  override def visitSection(ctx: SectionContext): Block = {
    val header   = ctx.header().label().getText
    val mappings = ctx.mapping().asScala.map(visitMapping).toList
    Block(header, mappings)
  }

  /** Create the top-level document object. It will always contain at least one section named 'main' */
  override def visitDocument(ctx: DocumentContext): Document = {
    if (!ctx.section().isEmpty) {
      val sections = ctx
        .section()
        .asScala
        .map(visitSection)
        .map(section => section.name -> section)
        .toMap
      Document(sections)
    } else if (!ctx.mapping().isEmpty) {
      Document(
        Map(
          "main" -> Block("main", ctx.mapping().asScala.map(visitMapping).toList)
        ))
    } else {
      Document.empty
    }
  }

  /** Create a mapping definition */
  override def visitMapping(ctx: MappingContext): Rule = {
    if (ctx.assignment() != null) {
      visitAssignment(ctx.assignment())
    } else if (ctx.reassignment() != null) {
      visitReassignment(ctx.reassignment())
    } else if (ctx.variable() != null) {
      visitVariable(ctx.variable())
    } else if (ctx.rootAssignment() != null) {
      visitRootAssignment(ctx.rootAssignment())
    } else {
      throw new IllegalStateException()
    }
  }

  override def visitAssignment(ctx: AssignmentContext): Assignment = {
    val destination = ctx
      .destination()
      .label()
      .asScala
      .map(visitLabel)
      .map(_.name)
      .toList
    val pipeline = visitPipeline(ctx.pipeline())
    val positions = Positions(
      start = Position(ctx.start.getLine, ctx.start.getCharPositionInLine),
      end = Position(ctx.stop.getLine, ctx.stop.getCharPositionInLine)
    )
    Assignment(destination, pipeline, positions = Some(positions))
  }

  override def visitRootAssignment(ctx: RootAssignmentContext): Assignment = {
    val pipeline = visitPipeline(ctx.pipeline())
    Assignment(List.empty, pipeline)
  }

  override def visitVariable(ctx: VariableContext): Variable = {
    val destination = ctx
      .destination()
      .label()
      .asScala
      .map(visitLabel)
      .map(_.name)
      .toList
    val pipeline = visitPipeline(ctx.pipeline())
    Variable(destination, pipeline)
  }

  override def visitReassignment(ctx: ReassignmentContext): Reassignment = {
    val destination = ctx
      .destination()
      .label()
      .asScala
      .map(visitLabel)
      .map(_.name)
      .toList
    val callChain = visitCallChain(ctx.callChain())
    Reassignment(destination, callChain)
  }

  override def visitCallChain(ctx: CallChainContext): Pipeline = {
    Pipeline(ctx.function().asScala.map(visitFunction).toList)
  }

  /** Create a pipeline stage. It might be one of many different types */
  def visitPipeline(ctx: PipelineContext): Pipeline = {
    ctx match {
      case a: AbsolutePathExpContext =>
        visitAbsolutePathExpression(a.absolutePathExpression())
      case tv: TempVariableExpContext =>
        visitTempVariableExpression(tv.tempVariableExpression())
      case l: LiteralExpContext => visitLiteralExpression(l.literalExpression())
      case r: RelativePathExpContext =>
        visitRelativePathExpression(r.relativePathExpression())
      case v: VariableExpContext =>
        visitVariableExpression(v.variableExpression())
      case tr: ThisRelativePathExpContext =>
        visitThisRelativePathExpression(tr.thisRelativePathExpression())
      case m: MatchExpContext =>
        visitMatchExp(m)
      case i: IfExpContext =>
        visitIfExp(i)
      case a: ArrayPathExpContext =>
        visitArrayPathExpression(a.arrayPathExpression())
      case o: ObjectPathExpContext =>
        visitObjectPathExpression(o.objectPathExpression())
      case d: DivisionContext       => visitDivision(d) :: Pipeline(Nil)
      case m: MultiplicationContext => visitMultiplication(m) :: Pipeline(Nil)
      case a: AdditionContext       => visitAddition(a) :: Pipeline(Nil)
      case s: SubtractionContext    => visitSubtraction(s) :: Pipeline(Nil)
      case _                        => throw new IllegalStateException()
    }
  }

  /* This is the atto stringLiteral but modified a bit */
  val stringParser: Parser[PString] = {
    import atto._, Atto._, cats._, cats.implicits._

    // Unicode escaped characters
    val unicode: Parser[Char] =
      string("\\u") ~> count(4, hexDigit).map(ds => Integer.parseInt(new String(ds.toArray), 16).toChar)

    def quotedString(outer: Char) = {
      // Unescaped characters
      val nesc: Parser[Char] =
        elem(c => c =!= '\\' && c =!= outer && !c.isControl)

      // Escaped characters
      val esc: Parser[Char] =
        (char('\\') *> char(outer)) |
          string("\\\\") >| '\\' |
          string("\\/") >| '/' |
          string("\\b") >| '\b' |
          string("\\f") >| '\f' |
          string("\\n") >| '\n' |
          string("\\r") >| '\r' |
          string("\\t") >| '\t'

      // Quoted strings
      char(outer) ~> many(nesc | esc | unicode).map(cs => new String(cs.toArray)) <~ char(outer)
    }
    val singleQuoted = quotedString('\'')
    val doubleQuoted = quotedString('"')
    val triple       = "\"\"\""
    val tripleQuoted = string(triple) *> manyUntil(unicode | anyChar, string(triple)).map(_.mkString(""))
    (singleQuoted | tripleQuoted | doubleQuoted).map(s => PString(s))
  }

  /** Decode a string from its parsed form */
  def decodeString(in: String): PString = {
    import atto._, Atto._, cats._, cats.implicits._
    stringParser.parseOnly(in).done.either.leftMap(e => new Throwable(s"Couldn't parse string literal: $e")).toTry.get
  }

  /** Create a literal value like a string or an int */
  override def visitLiteral(ctx: LiteralContext): Literal = {
    if (ctx.Int() != null) {
      val int = ctx.Int().getText.toInt
      // Negate the int if we had a minus sign in front
      ctx.Minus() match {
        case null => Literal(new PInt(int))
        case _    => Literal(new PInt(0 - int))
      }
    } else if (ctx.String() != null) {
      val str = decodeString(ctx.String().getText)
      Literal(str)
    } else if (ctx.Float() != null) {
      val float = ctx.Float().getText.toDouble
      // Negate the float if we had a minus sign in front
      ctx.Minus() match {
        case null => Literal(new PDouble(float))
        case _    => Literal(new PDouble(0 - float))
      }
    } else if (ctx.Boolean() != null) {
      ctx.Boolean().toString() match {
        case "true"  => Literal(PTrue)
        case "false" => Literal(PFalse)
        case _       => throw new IllegalStateException()
      }
    } else {
      throw new IllegalStateException()
    }
  }

  /** Traverse a predicate that may be one of many types */
  override def visitPredicate(ctx: PredicateContext): Predicate = {
    if (ctx.Underscore() != null) {
      Underscore
    } else if (ctx.unitary() != null) {
      visitUnitary(ctx.unitary())
    } else if (ctx.binary() != null) {
      visitBinary(ctx.binary())
    } else if (ctx.And() != null) {
      require(ctx.predicate(0) != null && ctx.predicate(1) != null)
      And(visitPredicate(ctx.predicate(0)), visitPredicate(ctx.predicate(1)))
    } else if (ctx.Or() != null) {
      require(ctx.predicate(0) != null && ctx.predicate(1) != null)
      Or(visitPredicate(ctx.predicate(0)), visitPredicate(ctx.predicate(1)))
    } else if (ctx.negation() != null) {
      visitNegation(ctx.negation())
    } else if (ctx.grouped() != null) {
      visitGrouped(ctx.grouped())
    } else {
      throw new IllegalStateException()
    }
  }

  /** Create a predicate from another predicate enclosed in a group */
  override def visitGrouped(ctx: GroupedContext): Predicate = {
    visitPredicate(ctx.predicate())
  }

  /** Create a predicate from a negation operator */
  override def visitNegation(ctx: NegationContext): Not = {
    Not(visitPredicate(ctx.predicate()))
  }

  /** Create a predicate from a unitary operator */
  override def visitUnitary(ctx: UnitaryContext): Predicate = {
    require(ctx.pipeline() != null)
    val pipeline = visitPipeline(ctx.pipeline())
    if (ctx.ExistsOp() != null) {
      Exists(pipeline)
    } else {
      throw new IllegalStateException()
    }
  }

  /** Create a predicate from a binary operator */
  override def visitBinary(ctx: BinaryContext): Predicate = {
    require(ctx.pipeline(0) != null && ctx.pipeline(1) != null)
    val left  = visitPipeline(ctx.pipeline(0))
    val right = visitPipeline(ctx.pipeline(1))
    val cs    = ctx.CaseSensitive() != null

    if (ctx.SubStrOp() != null) {
      Substring(left, right, cs)
    } else if (ctx.EqualsOp() != null) {
      Equals(left, right, cs)
    } else if (ctx.NotEqualsOp() != null) {
      NotEquals(left, right, cs)
    } else if (ctx.InOp() != null) {
      In(left, right, cs)
    } else if (ctx.ContainsOp() != null) {
      new Contains(left, right, cs)
    } else if (ctx.LessThanOp() != null) {
      LessThan(left, right, cs)
    } else if (ctx.GreaterThanOp() != null) {
      GreaterThan(left, right, cs)
    } else if (ctx.LessThanOrEqualOp() != null) {
      LessThanOrEqual(left, right, cs)
    } else if (ctx.GreaterThanOrEqualOp() != null) {
      GreaterThanOrEqual(left, right, cs)
    } else {
      throw new IllegalStateException()
    }
  }

  /** Create an Maths for divison */
  override def visitDivision(ctx: DivisionContext): Expression = {
    Maths(visitPipeline(ctx.pipeline(0)), "/", visitPipeline(ctx.pipeline(1)))
  }

  /** Create an Maths for multiplication */
  override def visitMultiplication(ctx: MultiplicationContext): Expression = {
    Maths(visitPipeline(ctx.pipeline(0)), "*", visitPipeline(ctx.pipeline(1)))
  }

  /** Create an Maths for addition */
  override def visitAddition(ctx: AdditionContext): Expression = {
    Maths(visitPipeline(ctx.pipeline(0)), "+", visitPipeline(ctx.pipeline(1)))
  }

  /** Create an Maths for subtraction */
  override def visitSubtraction(ctx: SubtractionContext): Expression = {
    Maths(visitPipeline(ctx.pipeline(0)), "-", visitPipeline(ctx.pipeline(1)))
  }

  override def visitIfExpression(ctx: IfExpressionContext): If = {
    If(visitPredicate(ctx.predicate()), visitPipeline(ctx.pipeline(0)), Option(ctx.pipeline(1)).map(visitPipeline))
  }

  override def visitIfExp(ctx: IfExpContext): Pipeline = {
    Pipeline(List(visitIfExpression(ctx.ifExpression())))
  }

  /**
    * Visit a parse tree produced by the {@code matchExp}
    * labeled alternative in {@link MappingParser#pipeline}.
    *
    * @param ctx the parse tree
    * @return the visitor result
    */
  override def visitMatchExp(ctx: MatchExpContext): Pipeline = {
    Pipeline(List(visitMatch(ctx.`match`())))
  }

  /**
    * Visit a parse tree produced by {@link MappingParser#caseBlock}.
    *
    * @param ctx the parse tree
    * @return the visitor result
    */
  override def visitCaseBlock(ctx: CaseBlockContext): Case = {
    Case(visitPredicate(ctx.predicate()), visitPipeline(ctx.pipeline()))
  }

  /**
    * Visit a parse tree produced by {@link MappingParser#match}.
    *
    * @param ctx the parse tree
    * @return the visitor result
    */
  override def visitMatch(ctx: MatchContext): Match = {
    Match(visitPipeline(ctx.pipeline()), ctx.caseBlock().asScala.toList.map(visitCaseBlock))
  }
}
// scalastyle:on null
// scalastyle:on underscore.import
// scalastyle:on number.of.methods
