package io.idml.datanodes

import org.scalatest._

/** Test the behaviour of the PInt class */
class PIntTest extends FunSuite {

  // Int equality
  test("PInt(int min) == PInt(int min)")(PInt(Int.MinValue) === PInt(Int.MinValue))
  test("PInt(int max) == PInt(int max)")(PInt(Int.MaxValue) === PInt(Int.MaxValue))
  test("PInt(int max) != PInt(int min)")(PInt(Int.MaxValue) !== PInt(Int.MinValue))

  // Long equality
  test("PInt(long min) == PInt(long min)")(PInt(Long.MinValue) === PInt(Long.MinValue))
  test("PInt(long max) == PInt(long max)")(PInt(Long.MaxValue) === PInt(Long.MaxValue))
  test("PInt(long max) != PInt(long min)")(PInt(Long.MaxValue) !== PInt(Long.MinValue))

  // Ints and Longs
  test("PInt(int min) == PInt(long min)")(PInt(Long.MinValue) === PInt(Long.MinValue))
  test("PInt(int) == PInt(long)")(PInt(1000) === PInt(1000L))

  // Comparison with other numerical types
  test("PInt(int) == PDouble(double)")(PInt(1000) === PDouble(1000.0))
  test("PInt(int) == PDouble(float)")(PInt(1000) === PDouble(1000f))
  test("PInt(int) == PDouble(int)")(PInt(1000) === PDouble(1000))
  test("PInt(int) == PDouble(long)")(PInt(1000) === PDouble(1000L))

  // bool
  test("PInt(0) == PFalse")(PInt(0).bool() === PFalse)
  test("PInt(1) == PTrue")(PInt(1).bool() === PTrue)

  // to*Option
  test("toIntOption")(PInt(123).toIntOption === Some(123))
  test("toLongOption")(PInt(123).toLongOption === Some(123L))
  test("toBoolOption")(PInt(123).toBoolOption === None)
  test("toDoubleOption")(PInt(123).toDoubleOption === None)
  test("toFloatOption")(PInt(123).toDoubleOption === None)
  test("toStringOption")(PInt(123).toDoubleOption === None)
}
