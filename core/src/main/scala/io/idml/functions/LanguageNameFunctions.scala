package io.idml.functions
import io.idml.ast.Pipeline
import io.idml.datanodes.IString
import io.idml.{IdmlString, IdmlValue, InvalidCaller, InvalidParameters, MissingField}

import java.util.Locale

object LanguageNameFunctions {

  private def lookupLanguageWithLocale(code: String, targetLocale: Option[String]): IdmlValue = {
    val lang = Locale.forLanguageTag(code)
    targetLocale.fold(lang.getDisplayName)(t => lang.getDisplayName(Locale.forLanguageTag(t))) match {
      // Java can just return the original input if it didn't know, and we'll blank that out
      // if you do want the java behaviour, use (code.languageName() | code)
      case result if result == code => MissingField
      case result => IString(result)
    }
  }

  object LanguageNameFunction0 extends IdmlFunction0 {
    override protected def apply(cursor: IdmlValue): IdmlValue = cursor match {
      case s: IdmlString =>  lookupLanguageWithLocale(s.value, None)
      case _ => InvalidCaller
    }

    override def name: String = "languageName"
  }

  case class LanguageNameFunction1(arg: Pipeline) extends IdmlFunction1 {
    override protected def apply(cursor: IdmlValue, targetLocale: IdmlValue): IdmlValue = (cursor, targetLocale) match {
      case (s: IdmlString, t: IdmlString) => lookupLanguageWithLocale(s.value, Some(t.value))
      case (_: IdmlString, _) => InvalidParameters
      case _ => InvalidCaller
    }

    override def name: String = "languageName"
  }

}
