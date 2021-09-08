package io.idml
import java.util

import io.idml.datanodes.{IInt, IObject, IString}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

import scala.collection.JavaConverters._

class IdmlMappingSpec extends AnyFlatSpec with Matchers {

  "IdmlMapping" should "enable combination of mappings" in {
    val idml                    = Idml.autoBuilder().build()
    val mappings: List[Mapping] = List("a = 1", "b = 2", "a = 3").map(idml.compile)
    val combined                = Mapping.fromMultipleMappings(mappings.asJava)
    combined.run(IObject()) must equal(IObject("a" -> IInt(3), "b" -> IInt(2)))
  }

  "IdmlMapping" should "enable combination of mappings 2" in {
    val idml                    = Idml.autoBuilder().build()
    val mappings: List[Mapping] = List("a = 1", "b = 2", "c = 3").map(idml.compile)
    val combined                = Mapping.fromMultipleMappings(mappings.asJava)
    combined.run(IObject()) must equal(IObject("a" -> IInt(1), "b" -> IInt(2), "c" -> IInt(3)))
  }

  "IdmlMapping" should "work with nested fields" in {
    val idml = Idml.autoBuilder().build()
    val merged = Mapping.fromMultipleMappings(
      util.Arrays.asList[Mapping](
        idml.compile("interaction.content = text"),
        idml.compile("interaction.author = author"),
        idml.compile("interaction.content = real_text")
      )
    )
    val input = IObject("text" -> IString("an example body"), "author" -> IString("bob"), "real_text" -> IString("an alternate body"))
    merged.run(input) must equal(IObject("interaction" -> IObject("content" -> IString("an alternate body"), "author" -> IString("bob"))))
  }
}
