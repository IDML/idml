package io.idml

import io.idml.datanodes.{IObject, ITrue}
import org.scalatest.funsuite.AnyFunSuite

class IdmlChainItTest extends AnyFunSuite {

  test("Test mapping chain order works properly") {
    val idml   = Idml.autoBuilder().build()
    val chain  = idml.chain(
      idml.compile("x = a"),
      idml.compile("y = x \n z = a")
    )
    val output = chain.run(IObject("a" -> ITrue))

    assert(output.get("x") === ITrue)
    assert(output.get("z") === MissingField)
    assert(output.get("y") === ITrue)
  }

}
