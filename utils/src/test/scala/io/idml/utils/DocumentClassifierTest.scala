package io.idml.utils

import io.idml.utils.DocumentClassifier.DocumentClassifierVisitor
import io.idml.{FunctionResolverService, Idml, IdmlParser}
import org.scalatest.{MustMatchers, WordSpec}

/** Verify the DocumentClassifier */
class DocumentClassifierTest extends WordSpec with MustMatchers {

  /** Adds 'string.classify' syntactic sugar to make tests simpler */
  implicit class RuleRunner(in: String) {

    /** Classify a document string */
    def classify: DocumentType = {
      val doc = new IdmlParser().parse(new FunctionResolverService, in).nodes
      DocumentClassifier.classify(doc)
    }
  }

  /** Adds 'string.classifyAndReturnClassifier' syntactic sugar to make tests simpler */
  implicit class InternalsRuleRunner(in: String) {

    /** Classify a document string */
    def classifyAndReturnClassifier: DocumentClassifierVisitor = {
      val doc     = new IdmlParser().parse(new FunctionResolverService, in).nodes
      val visitor = new DocumentClassifierVisitor()
      visitor.visit(doc)
      visitor
    }
  }

  "DocumentClassifier.classify" when {
    "classifying a schema" should {
      "return SchemaDocumentType" in {
        "foo : required()".classify must equal(SchemaDocumentType)
        "foo : required()\nid: int()".classify must equal(SchemaDocumentType)
      }
    }

    "classifying a mapping" should {
      "return MappingDocumentType" in {
        "foo = bar".classify must equal(MappingDocumentType)
      }
    }

    "classifying a mixed document" should {
      "return MixedocumentType" in {
        "foo : required()\nfoo = bar".classify must equal(MixedDocumentType)
      }
    }
    "classifing a document with multiple sections" should {
      "classify mixed documents as MixedDocumentType" in {
        """[main]
          |x = apply("x")
          |[x]
          |x : required()""".stripMargin.classify must equal(MixedDocumentType)
      }
      "classify schema documents as SchemaDocumentType" in {
        """[main]
          |cat : required()
          |[foo]
          |dog : required()""".stripMargin.classify must equal(SchemaDocumentType)
      }
      "classify mapping documents as MappingDocumentType" in {
        """[main]
          |x = apply("x")
          |[x]
          |x = y""".stripMargin.classify must equal(MappingDocumentType)
      }
    }
  }

  "DocumentClassifierVisitor.visit" when {
    "counting assignments and resassignments" should {
      "cope with a single line schema" in {
        val ONE_LINE_SCHEMA = "foo : required()".classifyAndReturnClassifier
        ONE_LINE_SCHEMA.reassignments must equal(1)
        ONE_LINE_SCHEMA.assignments must equal(0)
      }
      "cope with a single line map" in {
        val ONE_LINE_MAP = "foo = bar".classifyAndReturnClassifier
        ONE_LINE_MAP.reassignments must equal(0)
        ONE_LINE_MAP.assignments must equal(1)
      }
      "cope with a multi-line schema" in {
        val MULTI_LINE_SCHEMA =
          """
          |foo : required()
          |id: int()""".stripMargin.classifyAndReturnClassifier
        MULTI_LINE_SCHEMA.reassignments must equal(2)
        MULTI_LINE_SCHEMA.assignments must equal(0)
      }
      "cope with a mixed document" in {
        val MIXED_DOC =
          """
          |foo : required()
          |foo = bar""".stripMargin.classifyAndReturnClassifier
        MIXED_DOC.reassignments must equal(1)
        MIXED_DOC.assignments must equal(1)
      }
      "cope with a map-heady mixed document" in {
        val MULTI_LINE_MAP_HEAVY_MIXED_DOCUMENT =
          """
          |foo : required()
          |foo = bar
          |baz = ohnoanexample""".stripMargin.classifyAndReturnClassifier
        MULTI_LINE_MAP_HEAVY_MIXED_DOCUMENT.assignments must equal(2)
        MULTI_LINE_MAP_HEAVY_MIXED_DOCUMENT.reassignments must equal(1)
      }
      "cope with a schema-heavy mixed document" in {
        val MULTI_LINE_SCHEMA_HEAVY_MIXED_DOCUMENT =
          """foo : int()
          |cat : required()
          |baz = ohnoanexample""".stripMargin.classifyAndReturnClassifier
        MULTI_LINE_SCHEMA_HEAVY_MIXED_DOCUMENT.reassignments must equal(2)
        MULTI_LINE_SCHEMA_HEAVY_MIXED_DOCUMENT.assignments must equal(1)
      }
      "cope with a schema-heavy mixed document with variables" in {
        val MULTI_LINE_SCHEMA_HEAVY_MIXED_DOCUMENT =
          """foo : int()
          |let foo = 1 + 2
          |cat : required()
          |baz = ohnoanexample""".stripMargin.classifyAndReturnClassifier
        MULTI_LINE_SCHEMA_HEAVY_MIXED_DOCUMENT.reassignments must equal(2)
        MULTI_LINE_SCHEMA_HEAVY_MIXED_DOCUMENT.assignments must equal(1)
      }
    }
    "invoked on a target with multiple sections" should {
      "count assignments and reassignments correctly across all sections" in {
        val MULTI_SECTION_MIXED_TEST_DOCUMENT = """
          |[main]
          |x = apply("x")
          |[x]
          |x : required()""".stripMargin
        MULTI_SECTION_MIXED_TEST_DOCUMENT.classifyAndReturnClassifier.assignments must equal(1)
        MULTI_SECTION_MIXED_TEST_DOCUMENT.classifyAndReturnClassifier.reassignments must equal(1)
      }
    }
  }
}
