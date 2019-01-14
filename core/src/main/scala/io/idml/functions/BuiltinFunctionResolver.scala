package io.idml.functions

import io.idml.datanodes.PString
import io.idml.ast._

import scala.reflect.runtime.{universe => ru}

/** Resolves language builtins */
class BuiltinFunctionResolver extends FunctionResolver {
  def resolve(name: String, args: List[Argument]): Option[PtolemyFunction] =
    (name, args) match {
      case ("apply", LiteralValue(PString(blockName)) :: Nil) =>
        Some(ApplyFunction(blockName))
      case ("applyArray", LiteralValue(PString(blockName)) :: Nil) =>
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
      case ("concat", LiteralValue(PString(sep)) :: Nil) =>
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
      case _ =>
        None
    }
  override def providedFunctions(): List[PtolemyFunctionMetadata] = List(
    PtolemyFunctionMetadata("apply", List("block" -> "block to apply"), "applies a block with the scope of the current expression"),
    PtolemyFunctionMetadata("applyArray",
                            List("block" -> "block to apply"),
                            "applies a block to an array with the scope of the current expression"),
    PtolemyFunctionMetadata("array",
                            List("expression" -> "transformation"),
                            "applies an expression with the scope of the current expression"),
    PtolemyFunctionMetadata("map",
                            List("expression" -> "transformation"),
                            "applies an expression with the scope of the current expression"),
    PtolemyFunctionMetadata("extract",
                            List("expression" -> "transformation"),
                            "applies an expression with the scope of the current expression"),
    PtolemyFunctionMetadata("average", List(), "averages the items in the list"),
    PtolemyFunctionMetadata("append", List("item"  -> "item to append"), "appends the item to this list"),
    PtolemyFunctionMetadata("prepend", List("item" -> "item to append"), "prepends the item to this list"),
    PtolemyFunctionMetadata("size", List.empty, "gets the size of a list or string"),
    PtolemyFunctionMetadata("concat",
                            List("separator" -> "string to separate the elements with"),
                            "turns a list into a string with separators between elements"),
    PtolemyFunctionMetadata("unique", List.empty, "returns all the unique items in this list, in the order they are encountered"),
    PtolemyFunctionMetadata("sort",
                            List("expression" -> "keying function"),
                            "sorts this list based on a keying function, will separate items of different types and drop nothings"),
    PtolemyFunctionMetadata("groupBy",
                            List("expression" -> "keying function"),
                            "groups a list by a keying function, returns an object with keys converted to strings"),
    PtolemyFunctionMetadata(
      "groupBySafe",
      List("expression" -> "keying function"),
      "groups a list by a keying function, returns a list of objects with the keys `values` and `key` to preserve the type of the key"
    ),
    PtolemyFunctionMetadata("filter",
                            List("predicate" -> "filtering predicate"),
                            "returns a new list with only the items which match the predicate")
  )
}
