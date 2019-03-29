package io.idml.test

import io.circe.Json
import io.circe.generic.auto._
import io.circe.literal.JsonStringContext
import io.circe.syntax._
import org.scalatest.{MustMatchers, WordSpec}

class MainSpec extends WordSpec with MustMatchers with CirceEitherEncoders {}
