package io.idml

/** Allows traversal of a PtolemyValue using the visitor pattern */
abstract class PtolemyValueVisitor {

  def visitObject(path: Seq[String], obj: PtolemyObject) {
    obj.fields.foreach {
      case (key, value) => visitValue(path :+ key, value)
    }
  }

  def visitArray(path: Seq[String], array: PtolemyArray) {
    array.items.foreach {
      case value: Any => visitValue(path, value)
    }
  }

  def visitNothing(path: Seq[String], n: PtolemyNothing) {}

  def visitString(path: Seq[String], s: PtolemyString) {}

  def visitDouble(path: Seq[String], d: PtolemyDouble) {}

  def visitInt(path: Seq[String], i: PtolemyInt) {}

  def visitBool(path: Seq[String], b: PtolemyBool) {}

  def visitNull(path: Seq[String]) {}

  def visitPrimitive(path: Seq[String], value: PtolemyValue) {
    value match {
      case v: PtolemyString => visitString(path, v)
      case v: PtolemyDouble => visitDouble(path, v)
      case v: PtolemyInt    => visitInt(path, v)
      case v: PtolemyBool   => visitBool(path, v)
      case PtolemyNull      => visitNull(path)
    }
  }

  def visitIndexValue(path: Seq[String], index: Int, value: PtolemyValue) {
    visitValue(path, value)
  }

  def visitValue(path: Seq[String], value: PtolemyValue) {
    value match {
      case o: PtolemyObject  => visitObject(path, o)
      case a: PtolemyArray   => visitArray(path, a)
      case n: PtolemyNothing => visitNothing(path, n)
      case v: Any            => visitPrimitive(path, v)
    }
  }

  def visitValue(value: PtolemyValue) {
    visitValue(Vector(), value)
  }
}
