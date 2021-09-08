package io.idml.geo
import io.idml.datanodes.IString
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must

class TimezoneSpec extends AnyWordSpec with must.Matchers {

  "the timezone function" should {
    "know the timezone of Reading" in {
      TimezoneFunction.query(Geo(51.459, -0.9722)) must equal(Some("Europe/London"))
    }
    "know the timezone of Christchurch, NZ" in {
      TimezoneFunction.query(Geo(-43.53, 172.62)) must equal(Some("Pacific/Auckland"))
    }
  }

}
