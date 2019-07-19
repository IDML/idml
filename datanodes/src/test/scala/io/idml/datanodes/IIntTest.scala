package io.idml.datanodes

import org.scalatest._

/** Test the behaviour of the PInt class */
class IIntTest extends FunSuite {

  // Int equality
  test("PInt(int min) == PInt(int min)")(IInt(Int.MinValue) === IInt(Int.MinValue))
  test("PInt(int max) == PInt(int max)")(IInt(Int.MaxValue) === IInt(Int.MaxValue))
  test("PInt(int max) != PInt(int min)")(IInt(Int.MaxValue) !== IInt(Int.MinValue))

  // Long equality
  test("PInt(long min) == PInt(long min)")(IInt(Long.MinValue) === IInt(Long.MinValue))
  test("PInt(long max) == PInt(long max)")(IInt(Long.MaxValue) === IInt(Long.MaxValue))
  test("PInt(long max) != PInt(long min)")(IInt(Long.MaxValue) !== IInt(Long.MinValue))

  // Ints and Longs
  test("PInt(int min) == PInt(long min)")(IInt(Long.MinValue) === IInt(Long.MinValue))
  test("PInt(int) == PInt(long)")(IInt(1000) === IInt(1000L))

  // Comparison with other numerical types
  test("PInt(int) == PDouble(double)")(IInt(1000) === IDouble(1000.0))
  test("PInt(int) == PDouble(float)")(IInt(1000) === IDouble(1000f))
  test("PInt(int) == PDouble(int)")(IInt(1000) === IDouble(1000))
  test("PInt(int) == PDouble(long)")(IInt(1000) === IDouble(1000L))

  // bool
  test("PInt(0) == PFalse")(IInt(0).bool() === IFalse)
  test("PInt(1) == PTrue")(IInt(1).bool() === ITrue)

  // to*Option
  test("toIntOption")(IInt(123).toIntOption === Some(123))
  test("toLongOption")(IInt(123).toLongOption === Some(123L))
  test("toBoolOption")(IInt(123).toBoolOption === None)
  test("toDoubleOption")(IInt(123).toDoubleOption === None)
  test("toFloatOption")(IInt(123).toDoubleOption === None)
  test("toStringOption")(IInt(123).toDoubleOption === None)
}
