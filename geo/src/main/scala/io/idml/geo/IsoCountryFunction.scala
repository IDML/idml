package io.idml.geo

import java.nio.charset.Charset

import io.idml.{PtolemyJson, PtolemyValue}
import io.idml.ast.Pipeline
import io.idml.functions.PtolemyFunction1
import com.google.common.io.Resources

/** Turns iso countries into country names */
class IsoCountryFunction(countries: => PtolemyValue, val arg: Pipeline) extends PtolemyFunction1 {

  override protected def apply(cursor: PtolemyValue, country: PtolemyValue): PtolemyValue = {
    countries.get(country)
  }

  override def name: String = "country"
}
