package io.idml.geo

import java.nio.charset.Charset

import io.idml.{IdmlJson, IdmlValue}
import io.idml.ast.Pipeline
import io.idml.functions.IdmlFunction1
import com.google.common.io.Resources

/** Turns iso countries into country names */
class IsoCountryFunction(countries: => IdmlValue, val arg: Pipeline) extends IdmlFunction1 {

  override protected def apply(cursor: IdmlValue, country: IdmlValue): IdmlValue = {
    countries.get(country)
  }

  override def name: String = "country"
}
