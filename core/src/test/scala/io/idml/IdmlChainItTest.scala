package io.idml

import io.idml.datanodes.{IObject, ITrue}
import org.scalatest.FunSuite

class IdmlChainItTest extends FunSuite {

  test("Test mapping chain order works properly") {
    val ptolemy = new Idml(new IdmlConf)
    val chain = ptolemy.newChain(
      ptolemy.fromString("x = a"),
      ptolemy.fromString("y = x \n z = a")
    )
    val output = chain.run(IObject("a" -> ITrue))

    assert(output.get("x") === ITrue)
    assert(output.get("z") === MissingField)
    assert(output.get("y") === ITrue)
  }

}
