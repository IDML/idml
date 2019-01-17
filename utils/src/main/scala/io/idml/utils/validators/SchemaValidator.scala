package io.idml.utils.validators

import io.idml.utils.visitor.{ExecNodeVisitor, StructureAgnosticVisitationStyle}
import io.idml.ast.Document

/**
  * Object which provides a validate function, allowing you to ask if a schema is valid
  */
object SchemaValidator {

  /**
    * Visitor which detects functions not allowed inside schemas
    */
  class SchemaValidatorVisitor extends ExecNodeVisitor with StructureAgnosticVisitationStyle {
    var valid = true

    override def visitFunc(ctx: ExecFuncContext): Unit = {
      if (ctx.expr.name == "extract") {
        valid = false
      }
    }

  }

  /**
    * Check whether a document is a valid Schema
    * @param doc parsed Document tree
    * @return boolean indicating if it's valid
    */
  def validate(doc: Document): Boolean = {
    val visitor = new SchemaValidatorVisitor()
    visitor.visit(doc)
    visitor.valid
  }
}
