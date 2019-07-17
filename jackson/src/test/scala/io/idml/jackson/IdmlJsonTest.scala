package io.idml.jackson.difftool

import java.nio.charset.Charset

import io.idml.IdmlJson
import io.idml.jackson.IdmlJackson
import org.scalatest.FunSuite

class IdmlJsonTest extends FunSuite {

  test("scala literal is equivalent to pile-of-poo byte array (sanity check)") {
    // The 'pile of poo', a 4-byte unicode character http://www.fileformat.info/info/unicode/char/1F4A9/index.htm
    val utf8PooBytes: Array[Byte] =
      Array('"'.toByte, 0xF0, 0x9F, 0x92, 0xA9, '"'.toByte).map(o => o.toByte.ensuring(o == 0 || _ != 0))
    assert(utf8PooBytes === "\"\uD83D\uDCA9\"".getBytes(Charset.forName("UTF-8")))
  }

  test("scala source pile of poo is serialized as a utf8 pile of poo") {
    assert(
      IdmlJackson.default
        .compact(IdmlJackson.default.parse("\"\uD83D\uDCA9\"")) === "\"\uD83D\uDCA9\"")
  }

  test("ascii escaped scala pile of poo is serialized as a utf8 pile of poo") {
    assert(
      IdmlJackson.default
        .compact(IdmlJackson.default.parse("\"\\uD83D\\uDCA9\"")) === "\"\uD83D\uDCA9\"")
  }

}
