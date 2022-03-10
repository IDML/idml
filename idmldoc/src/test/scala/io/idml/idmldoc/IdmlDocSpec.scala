package io.idml.idmldoc

import cats.effect._
import cats._
import cats.implicits._
import io.idml.doc.{Markdown, Runners}
import org.scalatest.matchers.must
import org.scalatest.wordspec.AnyWordSpec

class IdmlDocSpec extends AnyWordSpec with must.Matchers {

  "IdmlDoc" when {
    "running" should {
      "allow for silent json" in {
        val input = """# hello
        |1. world
        |this is some normal markdown in theory
        |```idml:input:silent
        |{"a": 2, "b": 2}
        |```
        |```idml:code
        |result = a + b
        |```""".stripMargin

        val doc      = Markdown.parse(input).get.value
        val result   = Markdown.render(Runners.run[IO](doc).unsafeRunSync())
        val expected = """# hello
        |1. world
        |this is some normal markdown in theory
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

      "pass through code blocks with no type" in {
        val input =
          """# hello
            |```
            |code
            |```
            |blah
          """.stripMargin

        val doc = Markdown.parse(input).get.value
        doc.size must equal(3)
      }

      "inline code blocks that are marked to be inlined" in {
        val input =
          """# comment
          |```idml:input
          |{"a": 1, "b": 2}
          |```
          |into
          |```idml:code:inline
          |result = a + b
          |```
          |blah
        """.stripMargin

        val doc    = Markdown.parse(input).get.value
        val result = Markdown.render(Runners.run[IO](doc).unsafeRunSync())
        result must equal(
          """# comment
          |```json
          |{"a": 1, "b": 2}
          |```
          |into
          |```idml
          |result = a + b # 3
          |```
          |blah
        """.stripMargin
        )
      }
    }
  }

}
