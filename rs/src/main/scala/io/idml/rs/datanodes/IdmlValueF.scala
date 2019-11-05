package io.idml.rs.datanodes

import cats._
import cats.implicits._
import higherkindness.droste.{Algebra, Basis, Coalgebra}
import io.idml._
import io.idml.datanodes._
import io.idml.datanodes.modules.DateModule
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter

object DataNodesF {

  sealed trait IdmlValueF[+F]
  object IdmlValueF {
    case class IdmlArrayF[F](items: List[F]) extends IdmlValueF[F]
    case class IdmlBoolF(value: Boolean) extends IdmlValueF[Nothing]
    case class IdmlDoubleF(value: Double) extends IdmlValueF[Nothing]
    case class IdmlIntF(value: Long) extends IdmlValueF[Nothing]
    case object IdmlNothingF extends IdmlValueF[Nothing]
    case object IdmlNullF extends IdmlValueF[Nothing]
    case class IdmlObjectF[F](items: Map[String, F]) extends IdmlValueF[F]
    case class IdmlStringF(value: String) extends IdmlValueF[Nothing]
    case class IDateF(dateVal: DateTime, format: DateTimeFormatter = DateModule.DefaultDateFormat) extends IdmlValueF[Nothing]

    implicit val idmlValueFFunctor: Functor[IdmlValueF] = new Functor[IdmlValueF] {
      def map[A, B](fa: IdmlValueF[A])(f: A => B): IdmlValueF[B] = fa match {
        case IdmlArrayF(items) => IdmlArrayF(items.map(f))
        case IdmlBoolF(value) => IdmlBoolF(value)
        case IdmlDoubleF(value) => IdmlDoubleF(value)
        case IdmlIntF(value) => IdmlIntF(value)
        case IdmlNothingF => IdmlNothingF
        case IdmlNullF => IdmlNullF
        case IdmlObjectF(items) => IdmlObjectF(items.map(_.map(f)))
        case IdmlStringF(value) => IdmlStringF(value)
        case IDateF(dateVal, format) => IDateF(dateVal, format)
      }
    }

    def project(i: IdmlValue): IdmlValueF[IdmlValue] = i match {
      case a: IdmlArray => IdmlArrayF(a.items.toList)
      case b: IdmlBool => IdmlBoolF(b.value)
      case d: IdmlDouble => IdmlDoubleF(d.value)
      case i: IdmlInt => IdmlIntF(i.value)
      case _: IdmlNothing => IdmlNothingF
      case IdmlNull => IdmlNullF
      case o: IdmlObject => IdmlObjectF(o.fields.toMap)
      case d: IDate => IDateF(d.dateVal, d.format)
      case s: IdmlString => IdmlStringF(s.value)
      case _ => IdmlNothingF
    }

    def embed(f: IdmlValueF[IdmlValue]): IdmlValue = f match {
      case IdmlArrayF(items) => IArray(items.toBuffer)
      case IdmlBoolF(value) => IBool(value)
      case IdmlDoubleF(value) => IDouble(value)
      case IdmlIntF(value) => IInt(value)
      case IdmlNothingF => MissingField // rather than match the whole thing out we're just using this
      case IdmlNullF => IdmlNull
      case IdmlObjectF(items) => IObject(items.toList:_*)
      case IDateF(value, format) => IDate(value, format)
      case IdmlStringF(value) => IString(value)
    }

    val idmlValueAlgebra: Algebra[IdmlValueF, IdmlValue] = Algebra(embed)
    val idmlValueCoalgebra: Coalgebra[IdmlValueF, IdmlValue] = Coalgebra(project)

    implicit val idmlValueBasis: Basis[IdmlValueF, IdmlValue] = Basis.Default(idmlValueAlgebra, idmlValueCoalgebra)
  }




}
