package io.idml.hashing
import io.idml.datanodes.{IInt, IObject, IString}
import io.idml.{IdmlContext, IdmlValue}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers

class HashingSpec extends AnyWordSpec with Matchers {

  "HashingFunctionResolver" can {
    List("md5", "sha1", "sha256", "sha512", "murmurHash3", "cityHash", "xxHash32", "xxHash64")
      .foreach { hash =>
        s"resolve $hash" in {
          (new HashingFunctionResolver).resolve(hash, Nil) must equal(
            Some(HashingFunctions.hashes(hash)))
        }
      }

    "hash some prehashed strings with md5" should {
      Map(
        "hello"                        -> "5d41402abc4b2a76b9719d911017c592",
        "HELLO"                        -> "eb61eead90e3b899c6bcbe27ac581660",
        "123"                          -> "202cb962ac59075b964b07152d234b70",
        "hello world"                  -> "5eb63bbbe01eeed093cb22bb8f5acdc3",
        "antidisestablishmentarianism" -> "2a3ec66488847e798c29e6b500a1bcc6"
      ).foreach { case (input, output) =>
        s"match $input" in {
          val ctx = new IdmlContext(IObject("x" -> IString(input)))
          ctx.cursor = ctx.input.get("x")
          HashingFunctions.hashes("md5").invoke(ctx)
          ctx.cursor must equal(IString(output))
        }
      }
    }

    "hash some prehashed strings with xxHash32 and turn them into ints" should {
      Map(
        "hello"                        -> 4211111929L,
        "HELLO"                        -> 2521860973L,
        "123"                          -> 3062191159L,
        "hello world"                  -> 3468387874L,
        "antidisestablishmentarianism" -> 2071757008L
      ).foreach { case (input, output) =>
        s"match $input" in {
          val ctx = new IdmlContext(IObject("x" -> IString(input)))
          ctx.cursor = ctx.input.get("x")
          HashingFunctions.hashes("xxHash32").invoke(ctx)
          ctx.cursor.parseHexUnsigned.int must equal(IInt(output))
        }
      }
    }
  }

}
