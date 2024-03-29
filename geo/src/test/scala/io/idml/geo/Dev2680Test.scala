package io.idml.geo

import io.idml.Idml
import io.idml.jackson.IdmlJackson
import org.scalatest.funsuite.AnyFunSuite

class Dev2680Test extends AnyFunSuite {

  test("DEV-2680: IndexOutOfBoundsException from empty field cleanup in Idml") {
    val ptolemy = Idml.autoBuilder().build()
    val chain   = ptolemy.chain(
      ptolemy.compile("""
          |interaction.subtype = "ollie"
          |interaction.type = "twitter"
          |interaction.content = ollie.text
          |interaction.author.id = ollie.user.id
          |interaction.author.name = ollie.user.name
          |interaction.id_old = "123456"
          |rawlinks = ollie.links
          |raw_links = ollie.links
        """.stripMargin),
      ptolemy.compile(
        """
          |behaviour.subtype : string()
          |behaviour.started_at : date()
          |behaviour.finished_at : date()
          |behaviour.duration: int()
          |behaviour.user.id : int()
          |behaviour.user.name : string()
          |behaviour.location.geo : geo()
          |behaviour.location.altitude : float()
          |behaviour.location.accuracy : float()
          |behaviour.location.source : string()
          |behaviour.location.is_roaming : bool()
        """.stripMargin
      )
    )
    val input   = IdmlJackson.default.parse(
      """
        |{
        |  "ollie": {
        |    "user": {
        |      "id": "abcd1234",
        |      "name": "Ollie Parsley 1430226271.3534"
        |    },
        |    "text": "Hello this is some text 1430226271.3534",
        |    "links": [
        |      "http://datasift.com",
        |      "http://ollieparsley.com"
        |    ]
        |  }
        |}
      """.stripMargin
    )

    chain.run(input)

  }

}
