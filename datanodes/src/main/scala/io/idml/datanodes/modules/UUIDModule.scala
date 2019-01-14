package io.idml.datanodes.modules

import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.security.{MessageDigest, NoSuchAlgorithmException}
import java.util.UUID

import io.idml.datanodes.{PArray, PEmail, PString}
import io.idml.utils.PtolemyUUID
import io.idml._

/** Adds UUID generation behaviour to data nodes */
trait UUIDModule {
  this: PtolemyValue =>

  def uuid3(): PtolemyValue = this match {
    case n: PtolemyString => PString(UUID.nameUUIDFromBytes(n.value.getBytes(Charset.defaultCharset())).toString)
    case o: PtolemyObject =>
      PString(UUID.nameUUIDFromBytes(o.serialize().toStringOption.get.getBytes(Charset.defaultCharset())).toString)
    case _ => CastUnsupported
  }

  def uuid5(): PtolemyValue = this match {
    case n: PtolemyString => PString(PtolemyUUID.nameUUIDFromBytes5(n.value.getBytes(Charset.defaultCharset())).toString)
    case o: PtolemyObject =>
      PString(PtolemyUUID.nameUUIDFromBytes5(o.serialize().toStringOption.get.getBytes(Charset.defaultCharset())).toString)
    case _ => CastUnsupported
  }
}
