package io.idml.idmldoc

import org.scalatest.{FlatSpec, MustMatchers}
import cats.effect._
import cats._
import cats.implicits._
import io.idml.doc.Runners
import org.commonmark.parser.Parser
import org.commonmark.renderer.Renderer

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
      |```
    """.stripMargin

    val doc = Parser.builder().build().parse(input)
    Runners.idmlRunner[IO].map{ r =>
      doc.accept(Runners.runnerRunner(r))
    }

    Renderer

  }

}
