package io.idml

import io.idml.datanodes._
import org.scalatest.FunSuite
import scala.collection.JavaConverters._

class UnmappedFieldsFinderItTest extends FunSuite {

  val listener = new UnmappedFieldsFinder
  val ptolemy  = new Ptolemy(new PtolemyConf, listeners = List[PtolemyListener](listener).asJava)

  test("There are no unmapped fields when the input object is empty") {
    val ctx   = new PtolemyContext(PObject())
    val chain = ptolemy.newChain(ptolemy.fromString("x = a"))
    chain.run(ctx)
    assert(ctx.output === PObject())
  }

  test("If a field is mapped then it doesn't end up in the unmapped field namespace") {
    val ctx   = new PtolemyContext(PObject("a" -> PTrue))
    val chain = ptolemy.newChain(ptolemy.fromString("x = a"))
    chain.run(ctx)
    assert(ctx.output === PObject("x" -> PTrue))
  }

  test("If a field is mapped then it doesn't end up in the unmapped field namespace - depth two") {
    val ctx   = new PtolemyContext(PObject("a" -> PObject("b" -> PTrue)))
    val chain = ptolemy.newChain(ptolemy.fromString("x.y = a.b"))
    chain.run(ctx)
    assert(ctx.output === PObject("x" -> PObject("y" -> PTrue)))
  }

  test("If a field isn't mapped then it should be in the unmapped field namespace") {
    val ctx   = new PtolemyContext(PObject("a" -> PTrue))
    val chain = ptolemy.newChain(ptolemy.fromString("x = b"))
    chain.run(ctx)
    assert(ctx.output === PObject("unmapped" -> PObject("a" -> PTrue)))
  }

  test("If a field isn't mapped then it should be in the unmapped field namespace - depth two") {
    val ctx   = new PtolemyContext(PObject("a" -> PObject("b" -> PTrue)))
    val chain = ptolemy.newChain(ptolemy.fromString("x.y = b"))
    chain.run(ctx)
    assert(ctx.output === PObject("unmapped" -> PObject("a" -> PObject("b" -> PTrue))))
  }

  test("If a field isn't mapped then it should be in the unmapped field namespace - depth two, with overlap") {
    pendingUntilFixed {
      val ctx =
        new PtolemyContext(PObject("a" -> PObject("b" -> PTrue, "c" -> PFalse)))
      val chain = ptolemy.newChain(ptolemy.fromString("x.y = a.b"))
      chain.run(ctx)
      assert(ctx.output === PObject("x" -> PObject("y" -> PTrue), "unmapped" -> PObject("a" -> PObject("c" -> PFalse))))
    }
  }

  test("Fields are treated as mapped if functions are called on them successfully") {
    val ctx =
      new PtolemyContext(PObject("a" -> PObject("b" -> PString("1234"))))
    val chain = ptolemy.newChain(ptolemy.fromString("x.y = a.b.int()"))
    chain.run(ctx)
    assert(
      ctx.output === PObject(
        "x" -> PObject("y" -> PInt(1234))
      ))
  }

  test("Fields are not treated as mapped if functions are called on them unsuccessfully") {
    val ctx   = new PtolemyContext(PObject("a" -> PObject("b" -> PString(":D"))))
    val chain = ptolemy.newChain(ptolemy.fromString("x.y = a.b.int()"))
    chain.run(ctx)
    assert(
      ctx.output === PObject(
        "unmapped" -> PObject("a" -> PObject("b" -> PString(":D")))
      ))
  }

  test("Function parameters are treated as mapped, one parameter") {
    val ctx = new PtolemyContext(PObject("a" -> PObject("b" -> PString("abc %s"), "c" -> PString("123"))))
    val chain =
      ptolemy.newChain(ptolemy.fromString("x.y = a.b.format(root.a.c)"))
    chain.run(ctx)
    assert(
      ctx.output === PObject(
        "x" -> PObject("y" -> PString("abc 123"))
      ))
  }

  test("Function parameters are treated as mapped, two parameters") {
    val ctx = new PtolemyContext(
      PObject(
        "a" -> PObject(
          "b" -> PString("%s abc %s"),
          "c" -> PString("123"),
          "d" -> PString("def")
        )))
    val chain = ptolemy.newChain(ptolemy.fromString("x.y = a.b.format(root.a.c, root.a.d)"))
    chain.run(ctx)
    assert(
      ctx.output === PObject(
        "x" -> PObject("y" -> PString("123 abc def"))
      ))
  }

  test("Function parameters are treated as unmapped if the function failed") {
    val ctx = new PtolemyContext(PObject("a" -> PObject("b" -> PTrue, "c" -> PString("123"))))
    val chain =
      ptolemy.newChain(ptolemy.fromString("x.y = a.b.format(root.a.c)"))
    chain.run(ctx)
    assert(
      ctx.output === PObject(
        "unmapped" -> PObject("a" -> PObject("b" -> PTrue, "c" -> PString("123")))
      ))
  }

  test("Function parameters are treated as unmapped if the whole expression failed") {
    val ctx = new PtolemyContext(PObject("a" -> PObject("b" -> PString("abc %s"), "c" -> PString("123"))))
    val chain =
      ptolemy.newChain(ptolemy.fromString("x.y = a.b.format(root.a.c).int()"))
    chain.run(ctx)
    assert(
      ctx.output === PObject(
        "unmapped" -> PObject("a" -> PObject("b" -> PString("abc %s"), "c" -> PString("123")))
      ))
  }

  test("Composite values are treated as mapped, regardless of whether their sub-components are referred to") {
    val ctx   = new PtolemyContext(PObject("a" -> PObject("b" -> PString("http://localhost/"))))
    val chain = ptolemy.newChain(ptolemy.fromString("x.y = a.b.url().host"))
    chain.run(ctx)
    assert(ctx.output === PObject("x" -> PObject("y" -> PString("localhost"))))
  }

  test("Calls without a prefix path don't break the stack: url()") {
    val ctx   = new PtolemyContext(PObject("a" -> PObject("b" -> PTrue)))
    val chain = ptolemy.newChain(ptolemy.fromString("x.y = url()"))
    chain.run(ctx)
    assert(ctx.output === PObject("unmapped" -> PObject("a" -> PObject("b" -> PTrue))))
  }

  test("Calls without a prefix path but with a composite don't break the stack: fn(fn()) => nil") {
    pending
  }

  /*

    test("Apply a binary function to a path of depth two with arguments of depth two from different ancestors: a.b.fn(c.d, e.f) => a.b, a.b.c.d, a.b.e.f") {
      pending
    }

    test("Apply a binary function to a path of depth two with arguments of depth two from the same ancestors: a.b.fn(c.d, c.e) => a.b, a.b.c.d, a.b.c.e") {
      pending
    }

    test("Apply a binary function at the top level: fn(a, b) => a, b") {
      pending
    }

    test("Variable paths are ignored: @a => nil") {
      pending
    }

    test("Apply a unitary function with an absolute path: a.fn(root.b) => a, b") {
      pending
    }

    test("Nested functions: a.fn(b.fn(c)) => a, a.b, a.b.c") {
      pending
    }

    test("Try them all: a.fn(root.b.c, @c.d, e.f) => a, b.c, a.e.f") {
      pending
    }

    test("Simple complex paths: a.url().b => a") {
      pending
    }

    test("Complex paths with relative paths: a.url().fn(b, c) => a") {
      pending
    }

    test("Complex paths with absolute paths: a.url().fn(root.b, c) => a") {
      pending
    }

    test("Simple non-complex paths: a.fn().b => a.b") {
      pending
    }

 */
}
