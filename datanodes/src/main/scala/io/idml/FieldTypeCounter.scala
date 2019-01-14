package io.idml

/** Counts fields of particular types */
case class FieldTypeCounter(var nothing: Int = 0,
                            var strings: Int = 0,
                            var doubles: Int = 0,
                            var ints: Int = 0,
                            var bools: Int = 0,
                            var objects: Int = 0,
                            var arrays: Int = 0,
                            var nulls: Int = 0)
    extends PtolemyValueVisitor {

  override def visitNothing(path: Seq[String], n: PtolemyNothing): Unit = {
    nothing += 1
    super.visitNothing(path, n)
  }

  override def visitString(path: Seq[String], s: PtolemyString): Unit = {
    strings += 1
    super.visitString(path, s)
  }

  override def visitDouble(path: Seq[String], d: PtolemyDouble): Unit = {
    doubles += 1
    super.visitDouble(path, d)
  }

  override def visitInt(path: Seq[String], i: PtolemyInt): Unit = {
    ints += 1
    super.visitInt(path, i)
  }

  override def visitBool(path: Seq[String], b: PtolemyBool): Unit = {
    bools += 1
    super.visitBool(path, b)
  }

  override def visitNull(path: Seq[String]): Unit = {
    nulls += 1
    super.visitNull(path)
  }

  override def visitObject(path: Seq[String], obj: PtolemyObject) {
    objects += 1
    super.visitObject(path, obj)
  }

  override def visitArray(path: Seq[String], array: PtolemyArray) {
    arrays += 1
    super.visitArray(path, array)
  }
}
