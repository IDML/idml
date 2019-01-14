package io.idml.datanodes

import java.net.URL

import io.idml.{MissingField, PtolemyString, PtolemyValue}

/** Represents a valid URL */
class PUrl(url: URL) extends PtolemyString with CompositeValue {

  /** The URL represented as a string */
  val value: String = url.toString

  /** Override get(..) to provide custom field accessors */
  override def get(name: String): PtolemyValue = name match {
    case "host"     => PtolemyValue(url.getHost)
    case "protocol" => PtolemyValue(url.getProtocol)
    case "query"    => PtolemyValue(url.getQuery)
    case "path"     => PtolemyValue(url.getPath)
    case _          => MissingField
  }

  override def toString: String = value
}
