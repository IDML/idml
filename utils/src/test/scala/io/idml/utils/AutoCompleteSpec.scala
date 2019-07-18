package io.idml.utils
import io.idml.datanodes.{IArray, IInt, IObject, IString}
import io.idml.jackson.IdmlJackson
import io.idml.{jackson, _}
import org.scalatest.words.ShouldVerb
import org.scalatest.{FlatSpec, MustMatchers}

import scala.collection.JavaConverters._
import org.scalatest.Matchers._

class AutoCompleteSpec extends FlatSpec with MustMatchers {
  val ptolemy =
    Idml.createStaticWithDefaults(IdmlJackson.default, _.withResolver(new AnalysisModule).build())

  "complete" should "complete base level keys" in {
    val in     = IObject("a" -> IInt(1), "b" -> IInt(2))
    val doc    = "result = root."
    val cursor = doc.length
    AutoComplete.complete(ptolemy)(in, doc, cursor) should contain theSameElementsAs List("a", "b")
  }
  "complete" should "complete base level keys with this" in {
    val in     = IObject("a" -> IInt(1), "b" -> IInt(2))
    val doc    = "result = "
    val cursor = doc.length
    AutoComplete.complete(ptolemy)(in, doc, cursor) should contain theSameElementsAs List("a", "b")
  }
  "complete" should "complete within a map" in {
    val in = IObject("xs" -> IArray(IObject("a" -> IInt(1)), IObject("b" -> IInt(2))))
    println(IdmlJackson.default.compact(in))
    val doc    = "result = root.xs.map()"
    val cursor = doc.length - 1
    AutoComplete.complete(ptolemy)(in, doc, cursor) should contain theSameElementsAs List("a", "b")
  }
  "complete" should "complete between blocks" in {
    val in = IObject("a" -> IObject("b" -> IObject("c" -> IString("d"))))
    val doc =
      """[main]
        |foo = 42
        |result = a.apply("a")
        |[a]
        |inner = "aye"
        |innerResult = this.""".stripMargin
    val cursor = doc.length
    AutoComplete.complete(ptolemy)(in, doc, cursor) should contain theSameElementsAs List("b")
  }

}
