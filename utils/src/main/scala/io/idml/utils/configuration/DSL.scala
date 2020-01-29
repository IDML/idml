package io.idml.utils.configuration

import io.idml.{Idml, IdmlBuilder, IdmlChain, Mapping}
import atto._
import Atto._
import cats._
import cats.data._
import cats.implicits._
import higherkindness.droste._
import higherkindness.droste.macros._
import higherkindness.droste.scheme._

/**
  * A small DSL that can be used to combine mappings, examples look like this:
  * a|b # a piped into b
  * a+b # a and b run on the same input then merged
  *
  * Note: | takes precedence over + so in `a|b+c` the `b` and `c` mappings are combined, and fed the output from `a`
  */
object DSL {
  @deriveFixedPoint sealed trait ConfigurationMapping
  object ConfigurationMapping {
    final case class SingleMapping(name: String)                                   extends ConfigurationMapping
    final case class Merged(lhs: ConfigurationMapping, rhs: ConfigurationMapping)  extends ConfigurationMapping
    final case class Chained(lhs: ConfigurationMapping, rhs: ConfigurationMapping) extends ConfigurationMapping

  }
  import ConfigurationMapping._
  import ConfigurationMapping.fixedpoint._

  val printer: AlgebraM[Eval, ConfigurationMappingF, String] = AlgebraM[Eval, ConfigurationMappingF, String] {
    case SingleMappingF(value) => Eval.later(value)
    case MergedF(l, r)         => Eval.later(s"($l + $r)")
    case ChainedF(l, r)        => Eval.later(s"($l | $r)")
  }

  object Lexer {
    sealed trait Token
    case class Text(value: String) extends Token
    case object Pipe               extends Token
    case object Plus               extends Token

    val chars = Set('+', '|')
    val text  = takeWhile1(!chars.contains(_)).map(Text).widen[Token]
    val pipe  = char('|').as(Pipe).widen[Token]
    val plus  = char('+').as(Plus).widen[Token]

    val lexer                                                                                  = many1(text | (pipe | plus))
    def lex[M[_]](s: String)(implicit AE: ApplicativeError[M, String]): M[NonEmptyList[Token]] = AE.fromEither(lexer.parseOnly(s).either)
  }

  object Parser {
    class SplitAtFirstX[X](x: X) {
      def unapply(xs: List[X]): Option[(List[X], List[X])] = {
        val idx = xs.indexOf(x)
        if (idx != -1) {
          val (a, b) = xs.splitAt(idx)
          Some((a, b.drop(1)))
        } else {
          None
        }
      }
    }
    object SplitAtFirstPipe extends SplitAtFirstX[Lexer.Token](Lexer.Pipe)
    object SplitAtFirstPlus extends SplitAtFirstX[Lexer.Token](Lexer.Plus)

    def parser[M[_]](implicit AE: ApplicativeError[M, String]) = CoalgebraM[M, ConfigurationMappingF, List[Lexer.Token]] { tokens =>
      val mode = tokens.find(_ == Lexer.Pipe).orElse(tokens.find(_ == Lexer.Plus))
      (mode, tokens) match {
        case (None, Lexer.Text(t) :: Nil)               => AE.pure[ConfigurationMappingF[List[Lexer.Token]]](SingleMappingF(t))
        case (Some(Lexer.Pipe), SplitAtFirstPipe(l, r)) => AE.pure[ConfigurationMappingF[List[Lexer.Token]]](ChainedF(l, r))
        case (Some(Lexer.Plus), SplitAtFirstPlus(l, r)) => AE.pure[ConfigurationMappingF[List[Lexer.Token]]](MergedF(l, r))
        case _                                          => AE.raiseError(s"Parse error, unexpected tokens: $tokens")
      }
    }
    def parse[M[_]: Monad](tokens: NonEmptyList[Lexer.Token])(implicit AE: ApplicativeError[M, String]): M[ConfigurationMapping] =
      anaM(parser[M]).apply(tokens.toList)

    def apply[M[_]](s: String)(implicit ME: MonadError[M, String]): M[ConfigurationMapping] =
      for {
        tokens <- Lexer.lex[M](s)
        ast    <- Parser.parse[M](tokens)
      } yield ast
  }

  def combiner[M[_]: Monad](f: String => M[Mapping]): AlgebraM[M, ConfigurationMappingF, Mapping] =
    AlgebraM[M, ConfigurationMappingF, Mapping] {
      case SingleMappingF(value) => f(value)
      case MergedF(l, r)         => Mapping.fromMultipleMappings(List(l, r)).pure[M].widen[Mapping]
      case ChainedF(l, r)        => new IdmlChain(l, r).pure[M].widen[Mapping]
    }

  def print(m: ConfigurationMapping) = cataM(printer).apply(m)

  def run[M[_]](f: String => M[Mapping])(implicit ME: MonadError[M, String]): String => M[Mapping] =
    (s: String) =>
      for {
        tokens   <- Lexer.lex[M](s)
        ast      <- Parser.parse[M](tokens)
        combined <- cataM(combiner[M](f)).apply(ast)
      } yield combined

}
