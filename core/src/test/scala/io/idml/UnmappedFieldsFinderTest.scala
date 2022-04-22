package io.idml

import java.net.URL
import io.idml.datanodes._
import io.idml.ast._
import io.idml.functions.ApplyFunction
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.mockito.MockitoSugar

import scala.collection.mutable

class UnmappedFieldsFinderTest extends AnyFunSuite with MockitoSugar {

  test("Companion object throws rt exception if the state is missing") {
    intercept[RuntimeException](UnmappedFieldsFinder.fromContext(new IdmlContext()))
  }

  test("Entering a chain duplicates the input into the unmapped field delta") {
    val finder = new UnmappedFieldsFinder()
    val ctx    = new IdmlContext(IObject("a" -> ITrue))

    finder.enterChain(ctx)

    assert(finder.state(ctx).unmappedFields === IObject("a" -> ITrue))
  }

  test(
    "Exiting a chain places the unmapped field delta into the output object if there are unmapped fields") {
    val finder = new UnmappedFieldsFinder()
    val ctx    = new IdmlContext(IObject("a" -> ITrue))

    finder.enterChain(ctx)
    finder.exitChain(ctx)

    assert(ctx.output === IObject("unmapped" -> IObject("a" -> ITrue)))
  }

  test(
    "Exiting a chain doesn't place the unmapped field delta into the output object if it's empty") {
    val finder = new UnmappedFieldsFinder()
    val ctx    = new IdmlContext(IObject())

    finder.enterChain(ctx)
    finder.exitChain(ctx)

    assert(ctx.output === IObject())
  }

  test("Exiting an assignment that returned nothing clears path parts") {
    val finder = new UnmappedFieldsFinder()
    val ctx    = new IdmlContext(IObject("a" -> ITrue))
    ctx.cursor = NoFields
    finder.state(ctx).unmappedFields = IObject("a" -> ITrue)
    finder.state(ctx).pathParts += List("a")

    finder.exitAssignment(ctx, mock[Assignment])

    assert(finder.state(ctx).pathParts === Nil)
    assert(finder.state(ctx).unmappedFields === IObject("a" -> ITrue))
  }

  test(
    "Exiting an assignment that returned something clears path parts and removes them from the unmapped field delta") {
    val finder = new UnmappedFieldsFinder()
    val ctx    = new IdmlContext(IObject("a" -> ITrue))
    finder.state(ctx).unmappedFields = IObject("a" -> ITrue)
    finder.state(ctx).pathParts += List("a")

    finder.exitAssignment(ctx, mock[Assignment])

    assert(finder.state(ctx).pathParts === Nil)
    assert(finder.state(ctx).unmappedFields === IObject())
  }

  test("Exiting a path part does nothing if the stack is empty") {
    val finder = new UnmappedFieldsFinder()
    val ctx    = new IdmlContext(IObject("a" -> ITrue))
    finder.state(ctx).unmappedFields = IObject("a" -> ITrue)

    finder.exitPath(ctx, mock[Field])

    assert(finder.state(ctx).unmappedFields === IObject("a" -> ITrue))
  }

  test(
    "Exiting a path part adds the path to the head of the stack if we're not in a composite type") {
    val finder = new UnmappedFieldsFinder()
    val ctx    = new IdmlContext(IObject("a" -> ITrue))
    finder
      .state(ctx)
      .pathStack
      .push(new UnmappedFieldsPath(ExecNavRelative, isComposite = false))

    finder.exitPath(ctx, Field("abc"))

    assert(finder.state(ctx).pathStack.head.path === Seq("abc"))
  }

  test(
    "Exiting a path part doesn't add the path to the head of the stack if we're inside a composite type") {
    val finder = new UnmappedFieldsFinder()
    val ctx    = new IdmlContext(IObject("a" -> ITrue))
    finder
      .state(ctx)
      .pathStack
      .push(new UnmappedFieldsPath(ExecNavRelative, isComposite = true))

    finder.exitPath(ctx, Field("abc"))

    assert(finder.state(ctx).pathStack.head.path === Seq())
  }

  test("Entering an absolute path pipeline will add a marker to the stack") {
    val finder = new UnmappedFieldsFinder()
    val ctx    = new IdmlContext(IObject("a" -> ITrue))

    finder.enterPipl(ctx, Pipeline(List(ExecNavAbsolute)))

    assert(finder.state(ctx).pathStack.head.navType === ExecNavAbsolute)
    assert(finder.state(ctx).pathStack.head.isComposite === false)
  }

  test("Entering a variable path pipeline will add a marker to the stack") {
    val finder = new UnmappedFieldsFinder()
    val ctx    = new IdmlContext(IObject("a" -> ITrue))

    finder.enterPipl(ctx, Pipeline(List(ExecNavVariable)))

    assert(finder.state(ctx).pathStack.head.navType === ExecNavVariable)
    assert(finder.state(ctx).pathStack.head.isComposite === false)
  }

  test("Entering a relative path pipeline will add a marker to the stack") {
    val finder = new UnmappedFieldsFinder()
    val ctx    = new IdmlContext(IObject("a" -> ITrue))

    finder.enterPipl(ctx, Pipeline(List(ExecNavRelative)))

    assert(finder.state(ctx).pathStack.head.navType === ExecNavRelative)
    assert(finder.state(ctx).pathStack.head.isComposite === false)
  }

  test(
    "Entering a relative path pipeline will add a marker to the stack and the isComposite value will cascade if true") {
    val finder = new UnmappedFieldsFinder()
    val ctx    = new IdmlContext(IObject("a" -> ITrue))

    finder
      .state(ctx)
      .pathStack
      .push(new UnmappedFieldsPath(ExecNavRelative, isComposite = true))

    finder.enterPipl(ctx, Pipeline(List(ExecNavRelative)))

    assert(finder.state(ctx).pathStack.head.navType === ExecNavRelative)
    assert(finder.state(ctx).pathStack.head.isComposite === true)
  }

  test(
    "Exiting a relative path accumulates all the path parts from other relative paths in the stack") {
    val finder = new UnmappedFieldsFinder()
    val ctx    = new IdmlContext(IObject("a" -> ITrue))
    val state  = finder.state(ctx)

    state.pathStack.push(new UnmappedFieldsPath(ExecNavRelative, path = mutable.Queue("a", "b")))
    state.pathStack.push(new UnmappedFieldsPath(ExecNavAbsolute, path = mutable.Queue("1", "2")))
    state.pathStack.push(new UnmappedFieldsPath(ExecNavRelative, path = mutable.Queue("c", "d")))
    state.pathStack.push(new UnmappedFieldsPath(ExecNavAbsolute, path = mutable.Queue("3", "4")))
    state.pathStack.push(new UnmappedFieldsPath(ExecNavRelative, path = mutable.Queue("e", "f")))

    finder.exitPipl(ctx, Pipeline(List(ExecNavRelative)))

    assert(state.pathParts === List(List("a", "b", "c", "d", "e", "f")))
    assert(state.pathStack.size === 4)
  }

  test(
    "Exiting a relative path accumulates all the path parts from other relative paths in the stack and ignores composites") {
    val finder = new UnmappedFieldsFinder()
    val ctx    = new IdmlContext(IObject("a" -> ITrue))
    val state  = finder.state(ctx)

    state.pathStack.push(new UnmappedFieldsPath(ExecNavRelative, path = mutable.Queue("a", "b")))
    state.pathStack.push(
      new UnmappedFieldsPath(ExecNavRelative, path = mutable.Queue("c", "d"), isComposite = true))

    finder.exitPipl(ctx, Pipeline(List(ExecNavRelative)))

    assert(state.pathParts === List(List("a", "b")))
    assert(state.pathStack.size === 1)
  }

  test("Exiting an absolute path accumulates all the path parts in the stack") {
    val finder = new UnmappedFieldsFinder()
    val ctx    = new IdmlContext(IObject("a" -> ITrue))
    val state  = finder.state(ctx)

    state.pathStack.push(new UnmappedFieldsPath(ExecNavAbsolute, path = mutable.Queue("c", "d")))
    state.pathStack.push(new UnmappedFieldsPath(ExecNavAbsolute, path = mutable.Queue("a", "b")))

    finder.exitPipl(ctx, Pipeline(List(ExecNavAbsolute)))

    assert(state.pathParts === List(List("a", "b")))
    assert(state.pathStack.size === 1)
  }

  test("Exiting a variable has no effect") {
    val finder = new UnmappedFieldsFinder()
    val ctx    = new IdmlContext(IObject("a" -> ITrue))
    val state  = finder.state(ctx)

    state.pathStack.push(new UnmappedFieldsPath(ExecNavVariable, path = mutable.Queue("a", "b")))

    finder.exitPipl(ctx, Pipeline(List(ExecNavVariable)))

    assert(state.pathParts === Nil)
    assert(state.pathStack.isEmpty)
  }

  test("Exiting a function sets isComposite to true if it's true on the cursor") {
    val finder = new UnmappedFieldsFinder()
    val ctx    = new IdmlContext(IObject("a" -> ITrue))
    val state  = finder.state(ctx)
    ctx.cursor = new IUrl(new URL("http://nowhere.local"))
      .ensuring(_.isInstanceOf[CompositeValue])

    state.pathStack.push(new UnmappedFieldsPath(ExecNavVariable, path = mutable.Queue("a", "b")))

    finder.exitFunc(ctx, ApplyFunction("fn"))

    assert(state.pathStack.head.isComposite)
  }

  test("Exiting a function doesn't set the isComposite to true if it's false on the cursor") {
    val finder = new UnmappedFieldsFinder()
    val ctx    = new IdmlContext(IObject("a" -> ITrue))
    val state  = finder.state(ctx)
    ctx.cursor = IString("abc").ensuring(!_.isInstanceOf[CompositeValue])

    state.pathStack.push(new UnmappedFieldsPath(ExecNavVariable, path = mutable.Queue("a", "b")))

    finder.exitFunc(ctx, ApplyFunction("fn"))

    assert(!state.pathStack.head.isComposite)
  }

  test("Do mathematical operations affect the stack here?") {
    pending
  }
}
