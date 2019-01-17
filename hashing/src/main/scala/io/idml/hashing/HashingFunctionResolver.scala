package io.idml.hashing
import java.nio.ByteBuffer

import io.idml.datanodes.{PInt, PString}
import io.idml.{InvalidCaller, PtolemyNull, PtolemyString, PtolemyValue}
import io.idml.ast.{Argument, Pipeline, PtolemyFunction, PtolemyFunctionMetadata}
import io.idml.functions.{FunctionResolver, PtolemyFunction0}
import com.google.common.hash.Hashing
import com.google.common.io.BaseEncoding
import com.google.common.primitives.{Ints, Longs}
import net.jpountz.xxhash.XXHashFactory
import net.openhft.hashing.LongHashFunction

import scala.util.Try

class HashingFunctionResolver extends FunctionResolver {
  override def resolve(name: String, args: List[Argument]): Option[PtolemyFunction] = args match {
    case Nil => HashingFunctions.hashes.get(name)
    case _   => None
  }
  override def providedFunctions(): List[PtolemyFunctionMetadata] =
    HashingFunctions.hashes.map {
      case (name, _) =>
        PtolemyFunctionMetadata(name, List.empty, s"hash the current object with $name and return the digest")
    }.toList
}

object HashingFunctions {
  private def hashFunction(hashname: String)(hash: String => PString): (String, PtolemyFunction0) = hashname -> new PtolemyFunction0 {
    override protected def apply(cursor: PtolemyValue): PtolemyValue = cursor match {
      case (s: PtolemyString) =>
        Try {
          hash(s.value)
        }.getOrElse(PtolemyNull)
      case _ => InvalidCaller
    }
    override def name: String = hashname
  }

  implicit class BytesToHex(bs: Array[Byte]) {
    def toHex: String = BaseEncoding.base16().encode(bs).toLowerCase
  }

  val hashes: Map[String, PtolemyFunction0] = Map(
    hashFunction("xxHash32")((s: String) =>
      PString(Ints.toByteArray(XXHashFactory.safeInstance().hash32().hash(ByteBuffer.wrap(s.getBytes), 0)).toHex)),
    hashFunction("xxHash64")((s: String) => PString(Longs.toByteArray(LongHashFunction.xx().hashBytes(s.getBytes)).toHex)),
    hashFunction("cityHash")((s: String) => PString(Longs.toByteArray(LongHashFunction.city_1_1().hashBytes(s.getBytes)).toHex)),
    hashFunction("murmurHash3")((s: String) => PString(Longs.toByteArray(LongHashFunction.murmur_3().hashBytes(s.getBytes)).toHex)),
    hashFunction("sha1")((s: String) => PString(Hashing.sha1().hashBytes(s.getBytes).toString)),
    hashFunction("sha256")((s: String) => PString(Hashing.sha256().hashBytes(s.getBytes).toString)),
    hashFunction("sha512")((s: String) => PString(Hashing.sha512().hashBytes(s.getBytes).toString)),
    hashFunction("md5")((s: String) => PString(Hashing.md5().hashBytes(s.getBytes).toString)),
    hashFunction("crc232")((s: String) => PString(Hashing.crc32().hashBytes(s.getBytes).toString))
  )
}
