package io.idml.idmldoc

import org.scalatest.{FlatSpec, MustMatchers}
import cats.effect._
import cats._
import cats.implicits._
import io.idml.doc.{Markdown, Runners}

class IdmlDocSpec extends FlatSpec with MustMatchers {

  "IdmlDoc" should "run IDML" in {
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

    val doc = Markdown.parse(input).get.value

    val result = Markdown.render(Runners.run[IO](doc).unsafeRunSync())

    println(result)


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

    println("===")
    println(expected)

    result must equal(expected)
  }

}
