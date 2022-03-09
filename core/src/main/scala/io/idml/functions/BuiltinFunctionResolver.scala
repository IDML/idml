package io.idml.functions

import io.idml.datanodes.IString
import io.idml.ast._

import scala.reflect.runtime.{universe => ru}

/** Resolves language builtins */
class BuiltinFunctionResolver extends FunctionResolver {
  def resolve(name: String, args: List[Argument]): Option[IdmlFunction] =
    (name, args) match {
      case ("apply", LiteralValue(IString(blockName)) :: Nil) =>
        Some(ApplyFunction(blockName))
      case ("applyArray", LiteralValue(IString(blockName)) :: Nil) =>
        Some(ApplyArrayFunction(blockName))
      case ("array", expression :: Nil) =>
        Some(ArrayFunction(expression))
      case ("extract", expression :: Nil) =>
        Some(ExtractFunction(expression))
      case ("map", expression :: Nil) =>
        Some(ExtractFunction(expression))
      case ("blacklist", expressions: List[Pipeline]) =>
        Some(BlacklistFunction(expressions))
      case ("average", Nil) =>
        Some(AverageFunction)
      case ("append", (expression: Pipeline) :: Nil) =>
        Some(AppendFunction(expression))
      case ("prepend", (expression: Pipeline) :: Nil) =>
        Some(PrependFunction(expression))
      case ("size", Nil) =>
        Some(GetSizeFunction)
      case ("size", (size: Pipeline) :: Nil) =>
        Some(SetSizeFunction(size))
      case ("concat", LiteralValue(IString(sep)) :: Nil) =>
        Some(ConcatFunction(sep))
      case ("unique", expression :: Nil) =>
        Some(UniqueFunction(expression))
      case ("sort", expression :: Nil) =>
        Some(SortFunction(expression))
      case ("groupBy", expression :: Nil) =>
        Some(GroupByFunction(expression))
      case ("groupBySafe", expression :: Nil) =>
        Some(GroupsByFunction(expression))
      case ("filter", (pred: Predicate) :: Nil) =>
        Some(FilterFunction(pred))
      case ("languageName", Nil) =>
        Some(LanguageNameFunctions.LanguageNameFunction0)
      case ("languageName", (targetLocale: Pipeline) :: Nil) =>
        Some(LanguageNameFunctions.LanguageNameFunction1(targetLocale))
      case _ =>
        None
    }
  override def providedFunctions(): List[IdmlFunctionMetadata] = List(
    IdmlFunctionMetadata("apply", List("block" -> "block to apply"), "applies a block with the scope of the current expression"),
    IdmlFunctionMetadata("applyArray",
                         List("block" -> "block to apply"),
                         "applies a block to an array with the scope of the current expression"),
    IdmlFunctionMetadata("array", List("expression" -> "transformation"), "applies an expression with the scope of the current expression"),
    IdmlFunctionMetadata("map", List("expression"   -> "transformation"), "applies an expression with the scope of the current expression"),
    IdmlFunctionMetadata("extract",
                         List("expression" -> "transformation"),
                         "applies an expression with the scope of the current expression"),
    IdmlFunctionMetadata("average", List(), "averages the items in the list"),
    IdmlFunctionMetadata("append", List("item"  -> "item to append"), "appends the item to this list"),
    IdmlFunctionMetadata("prepend", List("item" -> "item to append"), "prepends the item to this list"),
    IdmlFunctionMetadata("size", List.empty, "gets the size of a list or string"),
    IdmlFunctionMetadata("concat",
                         List("separator" -> "string to separate the elements with"),
                         "turns a list into a string with separators between elements"),
    IdmlFunctionMetadata("unique", List.empty, "returns all the unique items in this list, in the order they are encountered"),
    IdmlFunctionMetadata("sort",
                         List("expression" -> "keying function"),
                         "sorts this list based on a keying function, will separate items of different types and drop nothings"),
    IdmlFunctionMetadata("groupBy",
                         List("expression" -> "keying function"),
                         "groups a list by a keying function, returns an object with keys converted to strings"),
    IdmlFunctionMetadata(
      "groupBySafe",
      List("expression" -> "keying function"),
      "groups a list by a keying function, returns a list of objects with the keys `values` and `key` to preserve the type of the key"
    ),
    IdmlFunctionMetadata("filter",
                         List("predicate" -> "filtering predicate"),
                         "returns a new list with only the items which match the predicate"),
    IdmlFunctionMetadata("languageName", List.empty, "look up the name of this ISO language code"),
    IdmlFunctionMetadata("languageName", List("targetLocale" -> "language code to look up the name in"), "look up the name of this ISO language code")
  )
}
