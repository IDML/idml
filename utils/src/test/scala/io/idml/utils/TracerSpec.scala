package io.idml.utils
import io.idml.jackson.IdmlJackson
import io.idml.utils.Tracer.Annotator
import io.idml.{Idml, IdmlContext, IdmlJson, IdmlListener}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must

class TracerSpec extends AnyWordSpec with must.Matchers {

  val json: IdmlJson = IdmlJackson.default

  "the tracing annotator" should {
    "trace simple IDML" in {
      val p    = Idml.autoBuilder().build()
      val a    = new Annotator(json)
      val ctx  = new IdmlContext(IdmlJson.newObject(), IdmlJson.newObject(), List[IdmlListener](a))
      val idml = "result = 2 + 2"
      p.compile(idml).run(ctx)
      a.render(idml) must equal("result = 2 + 2 # 4")
    }

    "trace multi line IDML" in {
      val p    = Idml.autoBuilder().build()
      val a    = new Annotator(json)
      val ctx  = new IdmlContext(IdmlJson.newObject(), IdmlJson.newObject(), List[IdmlListener](a))
      val idml =
        """a = 1
          |b = 2
          |c = @a + @b
          |e = @d""".stripMargin
      p.compile(idml).run(ctx)
      a.render(idml) must equal("""a = 1 # 1
          |b = 2 # 2
          |c = @a + @b # 3
          |e = @d # """.stripMargin)
    }

    "trace multi section IDML" in {
      val p    = Idml.autoBuilder().build()
      val a    = new Annotator(json)
      val ctx  = new IdmlContext(IdmlJson.newObject(), IdmlJson.newObject(), List[IdmlListener](a))
      val idml =
        """[main]
          |result = apply("foo")
          |
          |[foo]
          |a = 1""".stripMargin
      p.compile(idml).run(ctx)
      a.render(idml) must equal("""[main]
          |result = apply("foo") # {"a":1}
          |
          |[foo]
          |a = 1 # 1""".stripMargin)
    }

    "cope with input and functions" in {
      val p    = Idml.autoBuilder().build()
      val a    = new Annotator(json)
      val ctx  = new IdmlContext(
        json.parse("""{"a": "hello", "b": "world"}"""),
        IdmlJson.newObject(),
        List[IdmlListener](a))
      val idml = """result = "%s %s".format(a, b)"""
      p.compile(idml).run(ctx)
      a.render(idml) must equal("""result = "%s %s".format(a, b) # "hello world"""")
    }

  }

}
