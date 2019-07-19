package io.idml.utils

import io.idml.utils.validators.{MappingValidator, SchemaValidator}
import io.idml.ast.Document
import io.idml.{FunctionResolverService, Idml, IdmlParser}

/** Validates IDML documents */
object DocumentValidator {

  /**
    * Ensures an IDML document is valid.
    *
    * If it returns successfully, the document is valid, otherwise it will throw an exception
    */
  def validate(str: String): Unit = {
    validate(new IdmlParser().parse(new FunctionResolverService, str).nodes)
  }

  /**
    * Ensures that an IDML document is valid.
    *
    * If it returns successfully, the document is valid, otherwise it will throw an exception
    */
  def validate(doc: Document): Unit = {
    DocumentClassifier.classify(doc) match {
      case MixedDocumentType =>
        throw new ClassificationException("Document cannot be both schemas and mappings")
      case SchemaDocumentType =>
        if (!SchemaValidator.validate(doc)) {
          throw new ClassificationException("Document is a schema but contains inappropriate content for a schema")
        }
      case MappingDocumentType =>
        if (!MappingValidator.validate(doc)) {
          throw new ClassificationException("Document is a mapping but contains inappropriate content for a mapping")
        }
    }
  }
}
