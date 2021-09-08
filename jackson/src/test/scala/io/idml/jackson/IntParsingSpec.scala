package io.idml.jackson

import io.idml.datanodes.IInt
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

class IntParsingSpec extends AnyFunSuite with Matchers {
  import IdmlJackson.default._

  // Parsing
  test("parse(int min)")(parse(Int.MinValue.toString) === IInt(Int.MinValue))
  test("parse(int max)")(parse(Int.MaxValue.toString) === IInt(Int.MaxValue))
  test("parse(long min)")(parse(Long.MinValue.toString) === IInt(Long.MinValue))
  test("parse(long max)")(parse(Long.MaxValue.toString) === IInt(Long.MaxValue))

  // Generation
  test("generate(int min)")(Int.MinValue.toString === compact(IInt(Int.MinValue)))
  test("generate(int max)")(Int.MaxValue.toString === compact(IInt(Int.MaxValue)))
  test("generate(long min)")(Long.MinValue.toString === compact(IInt(Long.MinValue)))
  test("generate(long max)")(Long.MaxValue.toString === compact(IInt(Long.MaxValue)))

}
