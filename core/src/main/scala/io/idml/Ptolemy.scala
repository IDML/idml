package io.idml

import scala.annotation.varargs
import scala.collection.JavaConverters._

/**
  * The source of all Ptolemy state
  */
class Ptolemy(val conf: PtolemyConf,
              parser: PtolemyParser,
              fileResolver: FileResolver,
              resourceResolver: ResourceResolver,
              val functionResolver: FunctionResolverService,
              val listeners: java.util.List[PtolemyListener]) {

  def this(conf: PtolemyConf, listeners: java.util.List[PtolemyListener]) {
    this(conf, new PtolemyParser, new FileResolver, new ResourceResolver, new FunctionResolverService, listeners)
  }

  def this(conf: PtolemyConf, resolver: FunctionResolverService) {
    this(conf, new PtolemyParser, new FileResolver, new ResourceResolver, resolver, List.empty[PtolemyListener].asJava)
  }

  def this(conf: PtolemyConf, listeners: java.util.List[PtolemyListener], resolver: FunctionResolverService) {
    this(conf, new PtolemyParser, new FileResolver, new ResourceResolver, resolver, listeners)
  }

  def this(conf: PtolemyConf) {
    this(conf, List.empty[PtolemyListener].asJava)
  }

  def this() {
    this(new PtolemyConf)
  }

  /**
    * Parse a string
    */
  def fromString(in: String): PtolemyMapping = {
    parser.parse(this, in)
  }

  /**
    * Resolve a file and parse it
    */
  def fromFile(path: String): PtolemyMapping = {
    fromString(fileResolver.resolveAndLoad(path))
  }

  /**
    * Resolve a resource and parse it
    */
  def fromResource(path: String): PtolemyMapping = {
    fromString(resourceResolver.resolveAndLoad(path))
  }

  /**
    * Create a new chain
    */
  @varargs
  def newChain(transforms: PtolemyMapping*): PtolemyChain = {
    new PtolemyChain(this, transforms: _*)
  }

}
