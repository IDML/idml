package io.idml.utils

import io.idml.utils.visitor.{ExecNodeVisitor, StructureAgnosticVisitationStyle}
import io.idml.ast.Document

trait DocumentType
object SchemaDocumentType                  extends DocumentType
object MappingDocumentType                 extends DocumentType
object MixedDocumentType                   extends DocumentType
class ClassificationException(msg: String) extends RuntimeException(msg)

/** Classifies documents */
object DocumentClassifier {

  class DocumentClassifierVisitor extends ExecNodeVisitor with StructureAgnosticVisitationStyle {
    var assignments   = 0
    var reassignments = 0

    override def visitAssign(ctx: ExecAssignContext): Unit = {
      assignments += 1
    }

    override def visitReassign(ctx: ExecReassignContext): Unit = {
      reassignments += 1
    }

    def classifyDocument(): DocumentType = {
      (assignments, reassignments) match {
        case (a, r) if (r > 0 && a == 0)  => SchemaDocumentType
        case (a, r) if (r > 0 && a > 0)   => MixedDocumentType
        case (a, r) if (r == 0 && a > 0)  => MappingDocumentType
        case (a, r) if (r == 0 && a == 0) =>
          throw new ClassificationException("Document contained no assignments or reassignments")
        case _                            =>
          throw new ClassificationException("Unable to classify document")
      }
    }
  }

  /** Classify an Document tree as a Mapping, Schema or Mixed document
    * @param doc
    *   parsed Document tree
    * @return
    *   any subclass of DocumentType
    */
  def classify(doc: Document): DocumentType = {
    val visitor = new DocumentClassifierVisitor()
    visitor.visit(doc)
    visitor.classifyDocument()
  }
}
