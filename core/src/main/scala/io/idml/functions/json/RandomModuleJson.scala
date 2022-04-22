package io.idml.functions.json

import io.idml._
import io.idml.ast.Pipeline
import io.idml.functions.{IdmlFunction0, IdmlFunction2}
import spire.random.rng.{MersenneTwister32, Utils}

import scala.util.hashing.MurmurHash3

class RandomModuleJson(json: IdmlJson) {
  private def seed(p: IdmlValue): Option[MersenneTwister32] =
    p match {
      case seed: IdmlString =>
        Some(
          MersenneTwister32
            .fromSeed(Utils.seedFromInt(624, MurmurHash3.stringHash(seed.value)), 624 + 1))
      case seed: IdmlInt    =>
        Some(MersenneTwister32.fromSeed(Utils.seedFromInt(624, seed.value.toInt), 624 + 1))
      case seed: IdmlObject =>
        Some(
          MersenneTwister32
            .fromSeed(Utils.seedFromInt(624, MurmurHash3.stringHash(json.compact(seed))), 624 + 1))
      case _                => None
    }

  private def random(cursor: IdmlValue, min: IdmlValue, max: IdmlValue): IdmlValue = {
    (seed(cursor), min, max) match {
      case (Some(r), min: IdmlInt, max: IdmlInt)       =>
        IdmlValue(r.nextLong(min.value, max.value))
      case (Some(r), min: IdmlDouble, max: IdmlDouble) =>
        IdmlValue(r.nextDouble(min.value, max.value))
      case (None, _, _)                                => InvalidCaller
      case _                                           => InvalidParameters
    }
  }

  private def random(cursor: IdmlValue): IdmlValue =
    seed(cursor) match {
      case Some(r) => IdmlValue(r.nextLong())
      case None    => InvalidCaller
    }

  def random2Function(a1: Pipeline, a2: Pipeline): IdmlFunction2 =
    new IdmlFunction2 {
      override def apply(cursor: IdmlValue, min: IdmlValue, max: IdmlValue): IdmlValue =
        random(cursor, min, max)
      override val name                                                                = "random"
      override val arg1: Pipeline                                                      = a1
      override val arg2: Pipeline                                                      = a2
    }

  val random0Function: IdmlFunction0 = new IdmlFunction0 {
    def apply(cursor: IdmlValue): IdmlValue = random(cursor)
    override val name                       = "random"
  }
}
