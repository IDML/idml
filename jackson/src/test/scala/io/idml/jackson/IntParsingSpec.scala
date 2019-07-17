package io.idml.jackson

import io.idml.datanodes.PInt
import org.scalatest.{FunSuite, MustMatchers}

class IntParsingSpec extends FunSuite with MustMatchers {
  import IdmlJackson.default._

  // Parsing
  test("parse(int min)")(parse(Int.MinValue.toString) === PInt(Int.MinValue))
  test("parse(int max)")(parse(Int.MaxValue.toString) === PInt(Int.MaxValue))
  test("parse(long min)")(parse(Long.MinValue.toString) === PInt(Long.MinValue))
  test("parse(long max)")(parse(Long.MaxValue.toString) === PInt(Long.MaxValue))

  // Generation
  test("generate(int min)")(Int.MinValue.toString === compact(PInt(Int.MinValue)))
  test("generate(int max)")(Int.MaxValue.toString === compact(PInt(Int.MaxValue)))
  test("generate(long min)")(Long.MinValue.toString === compact(PInt(Long.MinValue)))
  test("generate(long max)")(Long.MaxValue.toString === compact(PInt(Long.MaxValue)))

}
