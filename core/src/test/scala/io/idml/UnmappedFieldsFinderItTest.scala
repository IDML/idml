package io.idml

import io.idml.datanodes._
import org.scalatest.FunSuite
import scala.collection.JavaConverters._

class UnmappedFieldsFinderItTest extends FunSuite {

  val listener = new UnmappedFieldsFinder
  val ptolemy  = new Idml(new IdmlConf, listeners = List[IdmlListener](listener).asJava)

  test("There are no unmapped fields when the input object is empty") {
    val ctx   = new IdmlContext(IObject())
    val chain = ptolemy.newChain(ptolemy.fromString("x = a"))
    chain.run(ctx)
    assert(ctx.output === IObject())
  }

  test("If a field is mapped then it doesn't end up in the unmapped field namespace") {
    val ctx   = new IdmlContext(IObject("a" -> ITrue))
    val chain = ptolemy.newChain(ptolemy.fromString("x = a"))
    chain.run(ctx)
    assert(ctx.output === IObject("x" -> ITrue))
  }

  test("If a field is mapped then it doesn't end up in the unmapped field namespace - depth two") {
    val ctx   = new IdmlContext(IObject("a" -> IObject("b" -> ITrue)))
    val chain = ptolemy.newChain(ptolemy.fromString("x.y = a.b"))
    chain.run(ctx)
    assert(ctx.output === IObject("x" -> IObject("y" -> ITrue)))
  }

  test("If a field isn't mapped then it should be in the unmapped field namespace") {
    val ctx   = new IdmlContext(IObject("a" -> ITrue))
    val chain = ptolemy.newChain(ptolemy.fromString("x = b"))
    chain.run(ctx)
    assert(ctx.output === IObject("unmapped" -> IObject("a" -> ITrue)))
  }

  test("If a field isn't mapped then it should be in the unmapped field namespace - depth two") {
    val ctx   = new IdmlContext(IObject("a" -> IObject("b" -> ITrue)))
    val chain = ptolemy.newChain(ptolemy.fromString("x.y = b"))
    chain.run(ctx)
    assert(ctx.output === IObject("unmapped" -> IObject("a" -> IObject("b" -> ITrue))))
  }

  test("If a field isn't mapped then it should be in the unmapped field namespace - depth two, with overlap") {
    pendingUntilFixed {
      val ctx =
        new IdmlContext(IObject("a" -> IObject("b" -> ITrue, "c" -> IFalse)))
      val chain = ptolemy.newChain(ptolemy.fromString("x.y = a.b"))
      chain.run(ctx)
      assert(ctx.output === IObject("x" -> IObject("y" -> ITrue), "unmapped" -> IObject("a" -> IObject("c" -> IFalse))))
    }
  }

  test("Fields are treated as mapped if functions are called on them successfully") {
    val ctx =
      new IdmlContext(IObject("a" -> IObject("b" -> IString("1234"))))
    val chain = ptolemy.newChain(ptolemy.fromString("x.y = a.b.int()"))
    chain.run(ctx)
    assert(
      ctx.output === IObject(
        "x" -> IObject("y" -> IInt(1234))
      ))
  }

  test("Fields are not treated as mapped if functions are called on them unsuccessfully") {
    val ctx   = new IdmlContext(IObject("a" -> IObject("b" -> IString(":D"))))
    val chain = ptolemy.newChain(ptolemy.fromString("x.y = a.b.int()"))
    chain.run(ctx)
    assert(
      ctx.output === IObject(
        "unmapped" -> IObject("a" -> IObject("b" -> IString(":D")))
      ))
  }

  test("Function parameters are treated as mapped, one parameter") {
    val ctx = new IdmlContext(IObject("a" -> IObject("b" -> IString("abc %s"), "c" -> IString("123"))))
    val chain =
      ptolemy.newChain(ptolemy.fromString("x.y = a.b.format(root.a.c)"))
    chain.run(ctx)
    assert(
      ctx.output === IObject(
        "x" -> IObject("y" -> IString("abc 123"))
      ))
  }

  test("Function parameters are treated as mapped, two parameters") {
    val ctx = new IdmlContext(
      IObject(
        "a" -> IObject(
          "b" -> IString("%s abc %s"),
          "c" -> IString("123"),
          "d" -> IString("def")
        )))
    val chain = ptolemy.newChain(ptolemy.fromString("x.y = a.b.format(root.a.c, root.a.d)"))
    chain.run(ctx)
    assert(
      ctx.output === IObject(
        "x" -> IObject("y" -> IString("123 abc def"))
      ))
  }

  test("Function parameters are treated as unmapped if the function failed") {
    val ctx = new IdmlContext(IObject("a" -> IObject("b" -> ITrue, "c" -> IString("123"))))
    val chain =
      ptolemy.newChain(ptolemy.fromString("x.y = a.b.format(root.a.c)"))
    chain.run(ctx)
    assert(
      ctx.output === IObject(
        "unmapped" -> IObject("a" -> IObject("b" -> ITrue, "c" -> IString("123")))
      ))
  }

  test("Function parameters are treated as unmapped if the whole expression failed") {
    val ctx = new IdmlContext(IObject("a" -> IObject("b" -> IString("abc %s"), "c" -> IString("123"))))
    val chain =
      ptolemy.newChain(ptolemy.fromString("x.y = a.b.format(root.a.c).int()"))
    chain.run(ctx)
    assert(
      ctx.output === IObject(
        "unmapped" -> IObject("a" -> IObject("b" -> IString("abc %s"), "c" -> IString("123")))
      ))
  }

  test("Composite values are treated as mapped, regardless of whether their sub-components are referred to") {
    val ctx   = new IdmlContext(IObject("a" -> IObject("b" -> IString("http://localhost/"))))
    val chain = ptolemy.newChain(ptolemy.fromString("x.y = a.b.url().host"))
    chain.run(ctx)
    assert(ctx.output === IObject("x" -> IObject("y" -> IString("localhost"))))
  }

  test("Calls without a prefix path don't break the stack: url()") {
    val ctx   = new IdmlContext(IObject("a" -> IObject("b" -> ITrue)))
    val chain = ptolemy.newChain(ptolemy.fromString("x.y = url()"))
    chain.run(ctx)
    assert(ctx.output === IObject("unmapped" -> IObject("a" -> IObject("b" -> ITrue))))
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
