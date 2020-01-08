package io.idml

import io.idml.datanodes.modules._
import io.idml.datanodes._

/** The base class for all json values */
abstract class IdmlValue
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
    with DomModule

/** The base class for all json values */
object IdmlValue {

  /** Create a new PInt */
  def apply(v: Long): IdmlValue = new IInt(v)

  /** Create a new PDouble */
  def apply(v: Double): IdmlValue = new IDouble(v)

  /** Create a new IdmlValue from a string */
  def apply(v: String): IdmlValue =
    Option(v).map(new IString(_)).getOrElse(IdmlNull)

  /** Create a new PBool */
  def apply(v: Boolean): IdmlValue = if (v) ITrue else IFalse

  private def sortingClass(p: IdmlValue): Int = p match {
    case IdmlNull       => 1
    case _: IdmlNothing => 2
    case _: IdmlArray   => 3
    case _: IdmlBool    => 4
    // sort doubles and ints together
    case _: IdmlDouble => 5
    case _: IdmlInt    => 5
    case _: IdmlObject => 6
    case _: IdmlString => 7
    case _             => 8
  }

  /** typeclass instances */
  implicit val intOrdering: Ordering[IdmlInt] = { (a: IdmlInt, b: IdmlInt) =>
    Ordering[Long].compare(a.value, b.value)
  }
  implicit val stringOrdering: Ordering[IdmlString] = { (a: IdmlString, b: IdmlString) =>
    Ordering[String].compare(a.value, b.value)
  }
  implicit val boolOrdering: Ordering[IdmlBool] = { (a: IdmlBool, b: IdmlBool) =>
    Ordering[Boolean].compare(a.value, b.value)
  }

  implicit val doubleOrdering: Ordering[IdmlDouble] = { (a: IdmlDouble, b: IdmlDouble) =>
    Ordering[Double].compare(a.value, b.value)
  }

  implicit val nullOrdering: Ordering[IdmlNothing] = (a: IdmlNothing, b: IdmlNothing) => 0

  implicit val ptolemyValueOrdering = new Ordering[IdmlValue] {
    override def compare(x: IdmlValue, y: IdmlValue): Int = {
      (sortingClass(x) - sortingClass(y)) match {
        case i if i < 0 => -1
        case i if i > 0 => 1
        case i if i == 0 =>
          (x, y) match {
            case (x: IdmlInt, y: IdmlInt)       => Ordering[IdmlInt].compare(x, y)
            case (x: IdmlString, y: IdmlString) => Ordering[IdmlString].compare(x, y)
            case (x: IdmlBool, y: IdmlBool)     => Ordering[IdmlBool].compare(x, y)
            case (x: IdmlDouble, y: IdmlDouble) => Ordering[IdmlDouble].compare(x, y)
            case (x: IdmlInt, y: IdmlDouble)    => Ordering[IdmlDouble].compare(new IDouble(x.value.doubleValue()), y)
            case (x: IdmlDouble, y: IdmlInt)    => Ordering[IdmlDouble].compare(x, new IDouble(y.value.doubleValue()))
            case _                              => 0
          }
      }

    }
  }
}
