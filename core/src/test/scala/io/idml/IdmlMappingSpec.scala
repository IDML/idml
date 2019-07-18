package io.idml
import java.util

import io.idml.datanodes.{IInt, IObject, IString}
import org.scalatest.{FlatSpec, MustMatchers}

import scala.collection.JavaConverters._

class IdmlMappingSpec extends FlatSpec with MustMatchers {

  "IdmlMapping" should "enable combination of mappings" in {
    val ptolemy                 = new Idml()
    val mappings: List[Mapping] = List("a = 1", "b = 2", "a = 3").map(ptolemy.fromString)
    val combined                = Mapping.fromMultipleMappings(ptolemy, mappings.asJava)
    combined.run(IObject()) must equal(IObject("a" -> IInt(3), "b" -> IInt(2)))
  }

  "IdmlMapping" should "enable combination of mappings 2" in {
    val ptolemy                 = new Idml()
    val mappings: List[Mapping] = List("a = 1", "b = 2", "c = 3").map(ptolemy.fromString)
    val combined                = Mapping.fromMultipleMappings(ptolemy, mappings.asJava)
    combined.run(IObject()) must equal(IObject("a" -> IInt(1), "b" -> IInt(2), "c" -> IInt(3)))
  }

  "IdmlMapping" should "work with nested fields" in {
    val ptolemy = new Idml
    val merged = Mapping.fromMultipleMappings(
      ptolemy,
      util.Arrays.asList[Mapping](
        ptolemy.fromString("interaction.content = text"),
        ptolemy.fromString("interaction.author = author"),
        ptolemy.fromString("interaction.content = real_text")
      )
    )
    val input = IObject("text" -> IString("an example body"), "author" -> IString("bob"), "real_text" -> IString("an alternate body"))
    merged.run(input) must equal(IObject("interaction" -> IObject("content" -> IString("an alternate body"), "author" -> IString("bob"))))
  }
}
