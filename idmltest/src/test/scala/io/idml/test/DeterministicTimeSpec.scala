package io.idml.test
import io.idml.datanodes.{IDate, IObject}
import io.idml._
import io.idml.circe.IdmlCirce
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must

import scala.collection.JavaConverters._

class DeterministicTimeSpec extends AnyWordSpec with must.Matchers {
  "DeterministicTime" should {
    "override now" in {
      val p = Idml
        .staticBuilderWithDefaults(IdmlCirce)
        .withResolverPrepend(new DeterministicTime())
        .build()
      p.compile("result = now()").run(IdmlJson.newObject()) must equal(
        IObject("result" -> IDate(new DateTime(0, DateTimeZone.UTC))))
    }
    "override now with a specific value" in {
      val p = Idml
        .staticBuilderWithDefaults(IdmlCirce)
        .withResolverPrepend(new DeterministicTime(1552653180L))
        .build()
      p.compile("result = now()").run(IdmlJson.newObject()) must equal(
        IObject("result" -> IDate(new DateTime(1552653180, DateTimeZone.UTC)))
      )
    }
    "override microtime" in {
      val p = Idml
        .staticBuilderWithDefaults(IdmlCirce)
        .withResolverPrepend(new DeterministicTime())
        .build()
      p.compile("result = microtime()").run(IdmlJson.newObject()) must equal(
        IObject("result" -> IdmlValue(0)))
    }
    "override microtime with a specific value" in {
      val p = Idml
        .staticBuilderWithDefaults(IdmlCirce)
        .withResolverPrepend(new DeterministicTime(1552653180L))
        .build()
      p.compile("result = microtime()").run(IdmlJson.newObject()) must equal(
        IObject("result" -> IdmlValue(1552653180L * 1000)))
    }
  }
}
