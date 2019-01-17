package io.idml.utils
import io.idml.datanodes.{PArray, PInt, PObject, PString}
import io.idml._
import org.scalatest.words.ShouldVerb
import org.scalatest.{FlatSpec, MustMatchers}

import scala.collection.JavaConverters._
import org.scalatest.Matchers._

class AutoCompleteSpec extends FlatSpec with MustMatchers {
  val ptolemy = new Ptolemy(new PtolemyConf(),
                            new StaticFunctionResolverService(
                              (StaticFunctionResolverService.defaults.asScala ++ List(new AnalysisModule)).asJava
                            ))

  "complete" should "complete base level keys" in {
    val in     = PObject("a" -> PInt(1), "b" -> PInt(2))
    val doc    = "result = root."
    val cursor = doc.length
    AutoComplete.complete(ptolemy)(in, doc, cursor) should contain theSameElementsAs List("a", "b")
  }
  "complete" should "complete base level keys with this" in {
    val in     = PObject("a" -> PInt(1), "b" -> PInt(2))
    val doc    = "result = "
    val cursor = doc.length
    AutoComplete.complete(ptolemy)(in, doc, cursor) should contain theSameElementsAs List("a", "b")
  }
  "complete" should "complete within a map" in {
    val in = PObject("xs" -> PArray(PObject("a" -> PInt(1)), PObject("b" -> PInt(2))))
    println(PtolemyJson.compact(in))
    val doc    = "result = root.xs.map()"
    val cursor = doc.length - 1
    AutoComplete.complete(ptolemy)(in, doc, cursor) should contain theSameElementsAs List("a", "b")
  }
  "complete" should "complete between blocks" in {
    val in = PObject("a" -> PObject("b" -> PObject("c" -> PString("d"))))
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
