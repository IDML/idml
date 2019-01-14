package io.idml

import io.idml.datanodes.CompositeValue
import io.idml.ast.{Assignment, ExecNav, ExecNavAbsolute, ExecNavRelative, ExecNavVariable, Field, Maths, Pipeline, PtolemyFunction}
import org.slf4j.LoggerFactory

import scala.collection.mutable

/** Used to encapsulate the type of path and the path */
class UnmappedFieldsPath(val navType: ExecNav,
                         val path: mutable.Queue[String] = new mutable.Queue[String](),
                         var isComposite: Boolean = false)

/** The unmapped fields state */
class UnmappedFields(var unmappedFields: PtolemyValue = PtolemyNull,
                     val pathStack: mutable.Stack[UnmappedFieldsPath] = mutable.Stack(),
                     val pathParts: mutable.ListBuffer[List[String]] = mutable.ListBuffer())

/** Extract the state from the context */
object UnmappedFieldsFinder {
  def fromContext(ctx: PtolemyContext): UnmappedFields =
    ctx.state
      .getOrElse(classOf[UnmappedFieldsFinder],
                 throw new RuntimeException("Couldn't find state in the context. Are you sure this extension is activated?"))
      .asInstanceOf[UnmappedFields]
}

/** Discovers unmapped fields */
class UnmappedFieldsFinder extends PtolemyListenerWithState[UnmappedFields] {
  val logger = LoggerFactory.getLogger(getClass)

  val StackWasEmpty = "Stack was empty. Premature end?"

  protected def defaultState(ctx: PtolemyContext) = {
    new UnmappedFields()
  }

  override def enterChain(ctx: PtolemyContext): Unit = {
    try {
      // When we enter a chain it's time to start off with a clone of the original object
      state(ctx).unmappedFields = ctx.input.deepCopy
    } catch {
      case e: Exception => logger.warn("error in enterChain", e)
    }
  }

  override def exitChain(ctx: PtolemyContext): Unit = {
    try {
      // Add the unmapped fields to the unmapped fields namespace
      val s = state(ctx)
      if (!s.unmappedFields.isEmpty.value) {
        ctx.output.fields("unmapped") = s.unmappedFields
      }
    } catch {
      case e: Exception => logger.warn("error in exitChain", e)
    }
  }

  override def exitAssignment(ctx: PtolemyContext, assignment: Assignment): Unit = {
    try {
      // If we've just made an assignment
      if (!ctx.cursor.isInstanceOf[PtolemyNothing]) {
        val s = state(ctx)
        s.pathParts.foreach(s.unmappedFields.removeWithoutEmpty)
      }
      // Reset the current path for the next assignment
      state(ctx).pathParts.clear()
    } catch {
      case e: Exception => logger.warn("error in exitAssignment", e)
    }
  }

  override def exitMaths(ctx: PtolemyContext, maths: Maths): Unit = {
    // FIXME: This was originally the same as exitAssignment but I'm skeptical that's the right choice
  }

  override def exitPath(ctx: PtolemyContext, path: Field): Unit = {
    try {
      val s = state(ctx)
      // Let's make sure we are tracking paths inside this expression. For now only paths in assignments are tracked
      if (s.pathStack.nonEmpty) {
        // We've just visited a path, add it to the pathStack to say we've been there unless we're inside a composite object
        // like a url. For example, we want to track my_link.url().host as my_link, not my_link.host, which is a value that
        // doesn't exist in the input
        val head = s.pathStack.head
        if (!head.isComposite) {
          head.path.enqueue(path.name)
        }
      }
    } catch {
      case e: Exception => logger.warn("error in exitPath", e)
    }
  }

  override def enterPipl(ctx: PtolemyContext, pipl: Pipeline): Unit = {
    try {
      // TODO: Restructure AST so we have separate pipl types
      pipl.exps match {
        case ExecNavAbsolute :: tail =>
          state(ctx).pathStack.push(new UnmappedFieldsPath(ExecNavAbsolute))
        case ExecNavVariable :: tail =>
          state(ctx).pathStack.push(new UnmappedFieldsPath(ExecNavVariable))
        case ExecNavRelative :: tail =>
          // For relative paths we want to ensure the 'isComposite' flag cascades down to earlier levels
          val s = state(ctx)
          val isInsideCompositeValue =
            s.pathStack.headOption.exists(_.isComposite)
          s.pathStack.push(new UnmappedFieldsPath(ExecNavRelative, isComposite = isInsideCompositeValue))
        case _ => ()
      }
    } catch {
      case e: Exception => logger.warn("error in enterPipl", e)
    }
  }

  override def exitPipl(ctx: PtolemyContext, pipl: Pipeline): Unit = {
    try {
      // TODO: Restructure AST so we have separate pipl types
      pipl.exps match {
        case ExecNavAbsolute :: tail =>
          popAbsolutePath(ctx)
        case ExecNavRelative :: tail =>
          popRelativePath(ctx)
        case ExecNavVariable :: tail =>
          popVariablePath(ctx)
        case _ => ()
      }
    } catch {
      case e: Exception => logger.error("error in exitPipl", e)
    }
  }

  /** Called at end the of a relative path */
  protected def popRelativePath(ctx: PtolemyContext): Unit = {
    try {
      val s = state(ctx)
      require(s.pathStack.nonEmpty, StackWasEmpty)
      require(s.pathStack.last.navType == ExecNavRelative, s"Expected relative path end, got ${s.pathStack.last.navType}")

      val result = s.pathStack.foldLeft(List[String]()) {
        case (prev, item) if item.navType == ExecNavRelative && !item.isComposite =>
          // Join every non-composite relative path part into a single path
          item.path.toList ++ prev
        case (prev, _) =>
          // Ignore other paths types (absolute, literal)
          prev
      }

      // Get rid of the top of the stack, we're leaving
      s.pathStack.pop()

      if (result != Nil) {
        s.pathParts += result
      }
    } catch {
      case e: Exception => logger.warn("error in popRelativePath", e)
    }
  }

  /** Called at end the of an absolute path */
  protected def popAbsolutePath(ctx: PtolemyContext): Unit = {
    try {
      val s = state(ctx)
      require(s.pathStack.nonEmpty, StackWasEmpty)
      val result = s.pathStack.pop()
      require(result.navType == ExecNavAbsolute, s"Expected absolute path end, got ${result.navType}")

      // Add the result to the paths field
      if (result.path.nonEmpty) {
        s.pathParts += result.path.toList
      }
    } catch {
      case e: Exception => logger.warn("error in popAbsolutePath", e)
    }
  }

  /** Called at end the of a variable path */
  protected def popVariablePath(ctx: PtolemyContext): Unit = {
    try {
      val s = state(ctx)
      require(s.pathStack.nonEmpty, StackWasEmpty)
      val result = s.pathStack.pop()
      require(result.navType == ExecNavVariable, s"Expected variable path end, got ${result.navType}")
      // Do nothing, we don't care about variables
    } catch {
      case e: Exception => logger.warn("error in popVariablePath", e)
    }
  }

  override def exitFunc(ctx: PtolemyContext, func: PtolemyFunction): Unit = {
    try {
      val s = state(ctx)
      if (s.pathStack.nonEmpty && ctx.cursor.isInstanceOf[CompositeValue]) {
        // Mark the head as a rich type so we ignore all future paths
        s.pathStack.head.isComposite = true
      }
    } catch {
      case e: Exception => logger.warn("error in exitFunc", e)
    }
  }
}
