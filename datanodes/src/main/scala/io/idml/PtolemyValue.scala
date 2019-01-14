package io.idml

import io.idml.datanodes.modules._
import io.idml.datanodes._

/** The base class for all json values */
abstract class PtolemyValue
    extends SchemaModule
    with NavigationModule
    with StringModule
    with ObjectModule
    with UrlModule
    with DateModule
    with EmailModule
    with MathsModule
    with RegexModule
    with ArrayModule
    with RandomModule
    with UUIDModule

/** The base class for all json values */
object PtolemyValue {

  /** Create a new PInt */
  def apply(v: Long): PtolemyValue = new PInt(v)

  /** Create a new PDouble */
  def apply(v: Double): PtolemyValue = new PDouble(v)

  /** Create a new PtolemyValue from a string */
  def apply(v: String): PtolemyValue =
    Option(v).map(new PString(_)).getOrElse(PtolemyNull)

  /** Create a new PBool */
  def apply(v: Boolean): PtolemyValue = if (v) PTrue else PFalse

  private def sortingClass(p: PtolemyValue): Int = p match {
    case PtolemyNull       => 1
    case _: PtolemyNothing => 2
    case _: PtolemyArray   => 3
    case _: PtolemyBool    => 4
    // sort doubles and ints together
    case _: PtolemyDouble  => 5
    case _: PtolemyInt     => 5
    case _: PtolemyObject  => 6
    case _: PtolemyString  => 7
    case _                 => 8
  }

  /** typeclass instances */
  implicit val intOrdering: Ordering[PtolemyInt] = { (a: PtolemyInt, b: PtolemyInt) =>
    Ordering[Long].compare(a.value, b.value)
  }
  implicit val stringOrdering: Ordering[PtolemyString] = { (a: PtolemyString, b: PtolemyString) =>
    Ordering[String].compare(a.value, b.value)
  }
  implicit val boolOrdering: Ordering[PtolemyBool] = { (a: PtolemyBool, b: PtolemyBool) =>
    Ordering[Boolean].compare(a.value, b.value)
  }

  implicit val doubleOrdering: Ordering[PtolemyDouble] = { (a: PtolemyDouble, b: PtolemyDouble) =>
    Ordering[Double].compare(a.value, b.value)
  }

  implicit val nullOrdering: Ordering[PtolemyNothing] = (a: PtolemyNothing, b: PtolemyNothing) => 0

  implicit val ptolemyValueOrdering = new Ordering[PtolemyValue] {
    override def compare(x: PtolemyValue, y: PtolemyValue): Int = {
      (sortingClass(x) - sortingClass(y)) match {
        case i if i < 0 => -1
        case i if i > 0 => 1
        case i if i == 0 =>
          (x, y) match {
            case (x: PtolemyInt, y: PtolemyInt)       => Ordering[PtolemyInt].compare(x, y)
            case (x: PtolemyString, y: PtolemyString) => Ordering[PtolemyString].compare(x, y)
            case (x: PtolemyBool, y: PtolemyBool)     => Ordering[PtolemyBool].compare(x, y)
            case (x: PtolemyDouble, y: PtolemyDouble) => Ordering[PtolemyDouble].compare(x, y)
            case (x: PtolemyInt, y: PtolemyDouble)    => Ordering[PtolemyDouble].compare(new PDouble(x.value.doubleValue()), y)
            case (x: PtolemyDouble, y: PtolemyInt)    => Ordering[PtolemyDouble].compare(x, new PDouble(y.value.doubleValue()))
            case _                                    => 0
          }
      }

    }
  }
}
