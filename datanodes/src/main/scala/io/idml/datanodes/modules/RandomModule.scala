package io.idml.datanodes.modules

import io.idml._
import spire.implicits._
import spire.math._
import spire.random._
import spire.random.rng.{MersenneTwister32, Utils}

import scala.util.hashing.MurmurHash3

trait RandomModule {
  this: PtolemyValue =>

  private def seed(p: PtolemyValue): Option[MersenneTwister32] = this match {
    case seed: PtolemyString =>
      Some(MersenneTwister32.fromSeed(Utils.seedFromInt(624, MurmurHash3.stringHash(seed.value)), 624 + 1))
    case seed: PtolemyInt =>
      Some(MersenneTwister32.fromSeed(Utils.seedFromInt(624, seed.value.toInt), 624 + 1))
    case seed: PtolemyObject =>
      Some(MersenneTwister32.fromSeed(Utils.seedFromInt(624, MurmurHash3.stringHash(PtolemyJson.compact(seed))), 624 + 1))
    case _ => None
  }

  def random(min: PtolemyValue, max: PtolemyValue): PtolemyValue = {
    (seed(this), min, max) match {
      case (Some(r), min: PtolemyInt, max: PtolemyInt) =>
        PtolemyValue(r.nextLong(min.value, max.value))
      case (Some(r), min: PtolemyDouble, max: PtolemyDouble) =>
        PtolemyValue(r.nextDouble(min.value, max.value))
      case (None, _, _) => InvalidCaller
      case _            => InvalidParameters
    }
  }

  def random(): PtolemyValue = seed(this) match {
    case Some(r) => PtolemyValue(r.nextLong())
    case None    => InvalidCaller
  }

}
