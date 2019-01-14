package io.idml

import io.idml.datanodes.{PObject, PTrue}
import org.scalatest.FunSuite

class PtolemyChainItTest extends FunSuite {

  test("Test mapping chain order works properly") {
    val ptolemy = new Ptolemy(new PtolemyConf)
    val chain = ptolemy.newChain(
      ptolemy.fromString("x = a"),
      ptolemy.fromString("y = x \n z = a")
    )
    val output = chain.run(PObject("a" -> PTrue))

    assert(output.get("x") === PTrue)
    assert(output.get("z") === MissingField)
    assert(output.get("y") === PTrue)
  }

}
