package io.idml

import scala.annotation.varargs
import scala.collection.JavaConverters._

/**
  * The source of all Idml state
  */
class Idml(val conf: IdmlConf,
           parser: IdmlParser,
           fileResolver: FileResolver,
           resourceResolver: ResourceResolver,
           val functionResolver: FunctionResolverService,
           val listeners: java.util.List[IdmlListener]) {

  def this(conf: IdmlConf, listeners: java.util.List[IdmlListener]) {
    this(conf, new IdmlParser, new FileResolver, new ResourceResolver, new FunctionResolverService, listeners)
  }

  def this(conf: IdmlConf, resolver: FunctionResolverService) {
    this(conf, new IdmlParser, new FileResolver, new ResourceResolver, resolver, List.empty[IdmlListener].asJava)
  }

  def this(conf: IdmlConf, listeners: java.util.List[IdmlListener], resolver: FunctionResolverService) {
    this(conf, new IdmlParser, new FileResolver, new ResourceResolver, resolver, listeners)
  }

  def this(conf: IdmlConf) {
    this(conf, List.empty[IdmlListener].asJava)
  }

  def this() {
    this(new IdmlConf)
  }

  /**
    * Parse a string
    */
  def fromString(in: String): IdmlMapping = {
    parser.parse(this, in)
  }

  /**
    * Resolve a file and parse it
    */
  def fromFile(path: String): IdmlMapping = {
    fromString(fileResolver.resolveAndLoad(path))
  }

  /**
    * Resolve a resource and parse it
    */
  def fromResource(path: String): IdmlMapping = {
    fromString(resourceResolver.resolveAndLoad(path))
  }

  /**
    * Create a new chain
    */
  @varargs
  def newChain(transforms: IdmlMapping*): IdmlChain = {
    new IdmlChain(this, transforms: _*)
  }

}
