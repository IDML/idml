package io.idml.datanodes

import io.idml.IdmlNull
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

/** Test the functionality of the IdmlNull object */
class IdmlNullTest extends AnyFunSuite with Matchers {

  // Equality
  test("null == null")(IdmlNull must equal(IdmlNull))
}
