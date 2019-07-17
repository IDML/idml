package io.idml
import java.util

import io.idml.datanodes.{PInt, PObject, PString}
import org.scalatest.{FlatSpec, MustMatchers}

import scala.collection.JavaConverters._

class IdmlMappingSpec extends FlatSpec with MustMatchers {

  "IdmlMapping" should "enable combination of mappings" in {
    val ptolemy                 = new Idml()
    val mappings: List[Mapping] = List("a = 1", "b = 2", "a = 3").map(ptolemy.fromString)
    val combined                = Mapping.fromMultipleMappings(ptolemy, mappings.asJava)
    combined.run(PObject()) must equal(PObject("a" -> PInt(3), "b" -> PInt(2)))
  }

  "IdmlMapping" should "enable combination of mappings 2" in {
    val ptolemy                 = new Idml()
    val mappings: List[Mapping] = List("a = 1", "b = 2", "c = 3").map(ptolemy.fromString)
    val combined                = Mapping.fromMultipleMappings(ptolemy, mappings.asJava)
    combined.run(PObject()) must equal(PObject("a" -> PInt(1), "b" -> PInt(2), "c" -> PInt(3)))
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
    val input = PObject("text" -> PString("an example body"), "author" -> PString("bob"), "real_text" -> PString("an alternate body"))
    merged.run(input) must equal(PObject("interaction" -> PObject("content" -> PString("an alternate body"), "author" -> PString("bob"))))
  }
}
