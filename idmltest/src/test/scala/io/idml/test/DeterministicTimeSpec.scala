package io.idml.test
import io.idml.datanodes.{PDate, PObject}
import io.idml._
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.{MustMatchers, WordSpec}

import scala.collection.JavaConverters._

class DeterministicTimeSpec extends WordSpec with MustMatchers {
  "DeterministicTime" should {
    "override now" in {
      val p = new Ptolemy(
        new PtolemyConf(),
        new StaticFunctionResolverService((new DeterministicTime() :: StaticFunctionResolverService.defaults.asScala.toList).asJava)
      )
      p.fromString("result = now()").run(PtolemyJson.newObject()) must equal(PObject("result" -> PDate(new DateTime(0, DateTimeZone.UTC))))
    }
    "override now with a specific value" in {
      val p = new Ptolemy(
        new PtolemyConf(),
        new StaticFunctionResolverService((new DeterministicTime(1552653180) :: StaticFunctionResolverService.defaults.asScala.toList).asJava)
      )
      p.fromString("result = now()").run(PtolemyJson.newObject()) must equal(PObject("result" -> PDate(new DateTime(1552653180, DateTimeZone.UTC))))
    }
    "override microtime" in {
      val p = new Ptolemy(
        new PtolemyConf(),
        new StaticFunctionResolverService((new DeterministicTime() :: StaticFunctionResolverService.defaults.asScala.toList).asJava)
      )
      p.fromString("result = microtime()").run(PtolemyJson.newObject()) must equal(PObject("result" -> PtolemyValue(0)))
    }
    "override microtime with a specific value" in {
      val p = new Ptolemy(
        new PtolemyConf(),
        new StaticFunctionResolverService((new DeterministicTime(1552653180) :: StaticFunctionResolverService.defaults.asScala.toList).asJava)
      )
      p.fromString("result = microtime()").run(PtolemyJson.newObject()) must equal(PObject("result" -> PtolemyValue(1552653180L*1000)))
    }
  }
}
