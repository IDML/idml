package io.idml.idmldoc

import org.scalatest.{FlatSpec, MustMatchers, WordSpec}
import cats.effect._
import cats._
import cats.implicits._
import io.idml.doc.{Markdown, Runners}

class IdmlDocSpec extends WordSpec with MustMatchers {

  "IdmlDoc" when {
    "running" should {
      "run the IDML" in {
        val input = """# hello
        |1. world
        |this is some normal markdown in theory
        |
        |```python
        |print 2 + 2
        |```
        |
        |```idml:input
        |{"a": 2, "b": 2}
        |```
        |
        |```idml:code
        |result = a + b
        |```""".stripMargin

        val doc      = Markdown.parse(input).get.value
        val result   = Markdown.render(Runners.run[IO](doc).unsafeRunSync())
        val expected = """# hello
        |1. world
        |this is some normal markdown in theory
        |
        |```python
        |print 2 + 2
        |```
        |
        |```json
        |{"a": 2, "b": 2}
        |```
        |
        |```idml
        |result = a + b
        |```
        |```json
        |{
        |  "result" : 4
        |}
        |```""".stripMargin

        result must equal(expected)
      }

      "run silently when passed the silent flag" in {
        val input = """# hello
        |```idml:input
        |{"a": 2, "b": 2}
        |```
        |```idml:code:silent
        |result = a + b
        |```""".stripMargin

        val doc      = Markdown.parse(input).get.value
        val result   = Markdown.render(Runners.run[IO](doc).unsafeRunSync())
        val expected = """# hello
        |```json
        |{"a": 2, "b": 2}
        |```
        |```idml
        |result = a + b
        |```""".stripMargin

        result must equal(expected)
      }

      "fail the run when the IDML isn't valid" in {
        val input = """# hello
        |```idml:input
        |{"a": 2, "b": 2}
        |```
        |```idml:code:silent
        |result = a + // oh no it's invalid
        |```""".stripMargin

        val doc    = Markdown.parse(input).get.value
        val result = Runners.run[IO](doc).attempt.unsafeRunSync()
        result.isLeft must be(true)
      }
    }
  }

}
