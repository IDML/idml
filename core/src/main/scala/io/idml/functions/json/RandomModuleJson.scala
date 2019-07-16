package io.idml.functions.json

import io.idml._
import io.idml.ast.Pipeline
import io.idml.functions.{PtolemyFunction0, PtolemyFunction2}
import spire.random.rng.{MersenneTwister32, Utils}

import scala.util.hashing.MurmurHash3

class RandomModuleJson(json: PtolemyJson) {
  private def seed(p: PtolemyValue): Option[MersenneTwister32] = p match {
    case seed: PtolemyString =>
      Some(MersenneTwister32.fromSeed(Utils.seedFromInt(624, MurmurHash3.stringHash(seed.value)), 624 + 1))
    case seed: PtolemyInt =>
      Some(MersenneTwister32.fromSeed(Utils.seedFromInt(624, seed.value.toInt), 624 + 1))
    case seed: PtolemyObject =>
      Some(MersenneTwister32.fromSeed(Utils.seedFromInt(624, MurmurHash3.stringHash(json.compact(seed))), 624 + 1))
    case _ => None
  }

  private def random(cursor: PtolemyValue, min: PtolemyValue, max: PtolemyValue): PtolemyValue = {
    (seed(cursor), min, max) match {
      case (Some(r), min: PtolemyInt, max: PtolemyInt) =>
        PtolemyValue(r.nextLong(min.value, max.value))
      case (Some(r), min: PtolemyDouble, max: PtolemyDouble) =>
        PtolemyValue(r.nextDouble(min.value, max.value))
      case (None, _, _) => InvalidCaller
      case _            => InvalidParameters
    }
  }

  private def random(cursor: PtolemyValue): PtolemyValue = seed(cursor) match {
    case Some(r) => PtolemyValue(r.nextLong())
    case None    => InvalidCaller
  }

  def random2Function(a1: Pipeline, a2: Pipeline): PtolemyFunction2 = new PtolemyFunction2 {
    override def apply(cursor: PtolemyValue, min: PtolemyValue, max: PtolemyValue): PtolemyValue = random(cursor, min, max)
    override val name = "random"
    override val arg1: Pipeline = a1
    override val arg2: Pipeline = a2
  }

  val random0Function: PtolemyFunction0 = new PtolemyFunction0 {
    def apply(cursor: PtolemyValue): PtolemyValue = random(cursor)
    override val name = "random"
  }
}
