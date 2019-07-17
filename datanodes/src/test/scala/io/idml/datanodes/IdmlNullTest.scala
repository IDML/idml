package io.idml.datanodes

import io.idml.IdmlNull
import org.scalatest._

/** Test the functionality of the IdmlNull object */
class IdmlNullTest extends FunSuite with MustMatchers {

  // Equality
  test("null == null")(IdmlNull must equal(IdmlNull))
}
