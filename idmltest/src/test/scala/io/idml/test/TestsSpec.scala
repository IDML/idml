package io.idml.test
import io.circe.Json
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.literal.JsonStringContext

class TestsSpec extends AnyWordSpec with must.Matchers with CirceEitherEncoders {

  val testJson = json"""
      {
        "name" : "example test",
        "code" : "r = a + b",
        "input" : {
           "a" : 2,
           "b" : 2
        },
        "output" : {
        },
        "time" : null,
        "original" : null
      }"""

  val test = Test[Either[Ref, ?], Either[Ref, ?], Json](
    "example test",
    Right(Left("r = a + b")),
    Right(
      Json.obj(
        "a" -> Json.fromInt(2),
        "b" -> Json.fromInt(2)
      )
    ),
    Right(Json.obj()),
    None
  )

  "the Tests encoder and decoder" should {
    "decode single tests" in {
      testJson.as[Tests[Json]] must equal(
        Right(
          Tests(
            List(
              test
            )
          )
        )
      )
    }
    "decode arrays of tests" in {
      Json.arr(testJson, testJson).as[Tests[Json]] must equal(
        Right(
          Tests(
            List(
              test,
              test
            )
          )
        )
      )
    }
    "encode single tests" in {
      test.asJson must equal(testJson)
    }
    "encode arrays of tests" in {
      List(test, test).asJson must equal(Json.arr(testJson, testJson))
    }
  }

}
