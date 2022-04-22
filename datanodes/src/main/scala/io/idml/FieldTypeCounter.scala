package io.idml

/** Counts fields of particular types */
case class FieldTypeCounter(
    var nothing: Int = 0,
    var strings: Int = 0,
    var doubles: Int = 0,
    var ints: Int = 0,
    var bools: Int = 0,
    var objects: Int = 0,
    var arrays: Int = 0,
    var nulls: Int = 0
) extends IdmlValueVisitor {

  override def visitNothing(path: Seq[String], n: IdmlNothing): Unit = {
    nothing += 1
    super.visitNothing(path, n)
  }

  override def visitString(path: Seq[String], s: IdmlString): Unit = {
    strings += 1
    super.visitString(path, s)
  }

  override def visitDouble(path: Seq[String], d: IdmlDouble): Unit = {
    doubles += 1
    super.visitDouble(path, d)
  }

  override def visitInt(path: Seq[String], i: IdmlInt): Unit = {
    ints += 1
    super.visitInt(path, i)
  }

  override def visitBool(path: Seq[String], b: IdmlBool): Unit = {
    bools += 1
    super.visitBool(path, b)
  }

  override def visitNull(path: Seq[String]): Unit = {
    nulls += 1
    super.visitNull(path)
  }

  override def visitObject(path: Seq[String], obj: IdmlObject) {
    objects += 1
    super.visitObject(path, obj)
  }

  override def visitArray(path: Seq[String], array: IdmlArray) {
    arrays += 1
    super.visitArray(path, array)
  }
}
