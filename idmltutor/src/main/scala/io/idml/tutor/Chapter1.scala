package io.idml.tutor

import cats.effect.Sync
import fansi._
import io.idml.{Ptolemy, PtolemyValue}
import cats._
import cats.data.EitherT
import cats.implicits._
import cats.effect._
import cats.effect.implicits._
import io.circe.literal._
import io.circe.syntax._
import io.idml.circe._

object Chapter1 {
  import Colours._

  def apply[F[_]](tutorial: TutorialAlg[F])(implicit F: Sync[F]) = for {
    _ <- tutorial.title("Chapter 1: Introduction")
    _ <- tutorial.section("Section 1: Assignments")
    _ <- tutorial.content(
      s"""
        |IDML is a language for manipulating JSON, you'll usually be writing assignments, they look like this:
        |
        |  ${grey("foo = bar")}
        |
        |This means we should set the output field ${grey("foo")} to the result of the expression ${grey("bar")}
        |
        |On the right hand side of the equals, a reference like that would resolve to an item in the input object.
      """.stripMargin)
    _ <- tutorial.pause
    _ <- tutorial.section("Exercise 1: Assignments")
    _ <- tutorial.exercise(
      s"""
        |Map the text field over into the result field.
      """.stripMargin,
      json"""{"text":"hello"}""",
      json"""{"result":"hello"}"""
    )
    _ <- tutorial.section("Section 2: Navigation")
    _ <- tutorial.content(
      s"""
         |If you wanted to access or assign fields that are nested within other objects you'd use a dot to separate them like so:
         |
         |  ${grey("foo.bar = a.b.c")}
         |
         |This would set the ${grey("bar")} field inside ${grey("foo")} to the value of the ${grey("c")} field inside ${grey("b")} inside ${grey("a")}
         |
         |Similarly if we wanted to access a specific array field we'd use square brackets and an index like so:
         |
         |  ${grey("foo.bar = items[0]")}
         |
         |If one of the objects doesn't exist in the path we're looking for, the assignment will short circuit and not assign anything.
         |
         |If the ${grey("foo")} object didn't exist it would create it when assigning the ${grey("bar")} value inside it, but this would only occur if the right side evaluates to a value.
       """.stripMargin
    )
    _ <- tutorial.pause
    _ <- tutorial.section("Exercise 2: Navigation")
    _ <- tutorial.exercise(
      s"""
         |For this exercise we'll be mapping multiple fields, remember to hit enter twice once you're happy with your mapping.
       """.stripMargin,
      json"""{"tweet": {"text": "this is the body of a tweet #tweet", "hashtags": ["#tweet"]}}""",
      json"""{"event": {"text": "this is the body of a tweet #tweet", "first_hashtag": "#tweet"}}""",
      true
    )
    _ <- tutorial.section("Section 3: Functions")
    _ <- tutorial.content(
      s"""
        |Functions are one of the most important aspects of IDML, they're always called as a ${underlined("suffix")} to an expression, and always have rounded braces on the end.
        |
        |Example:
        |  ${grey("""text = "hello".capitalize()""")}
        |Output:
        |  ${grey("""{"text": "Hello"}""")}
        |
        |They can be chained, and will only be run if called on a node which exists.
      """.stripMargin
    )
    _ <- tutorial.pause
    _ <- tutorial.section("Exercise 3: Functions")
    _ <- tutorial.exercise(
      s"""
         |For this exercise you'll have two functions you can use:
         |
         |${underlined("strip")}: can be called on a string and will strip trailing whitespace
         |${underlined("int")}: can be called on a string to turn it into an integer
       """.stripMargin,
      json"""{"number": "123 "}""",
      json"""{"number":123}"""
    )
    _ <- tutorial.section("Section 4: Operators")
    _ <- tutorial.content(
      s"""
         |IDML provides some operators, which are mostly used for mathematical operations, they are called in an ${underlined("infix")} style like so:
         |  ${grey("result = a + b")}
       """.stripMargin)
    _ <- tutorial.pause
    _ <- tutorial.section("Exercise 4: Operators")
    _ <- tutorial.exercise(
      s"""
         |
         |
         |1. Add the words together to concatenate them
         |2. add the numbers together to add them
       """.stripMargin,
      json"""{"a": "hello ", "b": "world", "c": 1, "d": 2}""",
      json"""{"text": "hello world", "number": 3}""",
      multiline = true
    )
    _ <- tutorial.section("Exercise 4: Branching")
    _ <- tutorial.content(
     s"""
        |So far we've seen how to map content if it's available, but what if we want branching?
        |
        |IDML includes two basic tools for branching, coalescing and the if expression
        |
        |Coalescing allows you to fall through a list of paths if one doesn't exist:
        |
        |  ${grey("foo = (a | b)")}
        |
        |This would set ${grey("foo")} to the value of ${grey("a")} if it's defined, otherwise ${grey("b")} if it's defined, or not set it at all.
      """.stripMargin
    )
    _ <- tutorial.pause
    _ <- tutorial.content(
      s"""
         |If expressions rely on a part of the language we haven't touched yet, the predicate language.
         |
         |It's operator based and will evaluate to a boolean.
         |
         |Common usage examples of predicates include:
         |
         |  ${grey("""cat.name == "martin"""")}       ${green("# check if the cat's name is martin")}
         |  ${grey("""cat.name != "martin"""")}       ${green("# check if the cat's name is not martin")}
         |  ${grey("""cat.name in ["bob", "tom"]""")} ${green("# check if the cat's name is in our list of cat names")}
         |  ${grey("""cat.age > 5""")}                ${green("# check if the cat's age is above 5")}
         |  ${grey("""cat.name exists""")}            ${green("# check if the cat has a name")}
         |
         |These can be combined or modified using the ${grey("not")} prefix operator, or the ${grey("and")} and ${grey("or")} infix operators.
         |
         |For a full listing of supported predicates please see the docs at http://idml.io/features/predicates.html .
       """.stripMargin
    )
    _ <- tutorial.pause
    _ <- tutorial.content(
      s"""
         |the if expression looks like this:
         |
         |  ${grey("if foo then bar")} ${green("# evaluates to the result of the expression bar if the predicate foo evaluates to true")}
         |  ${grey("if foo then bar else baz")} ${green("# the same as above, but we fall through to the expression baz if it's false")}
         |
         |a practical example might be:
         |
         |  ${grey("""content = if type == "tweet" then tweet.content""")} ${green("# set the content to tweet.content if type is tweet")}
       """.stripMargin)
    _ <- tutorial.pause
    _ <- tutorial.section("Exercise 4: Branching")
    _ <- tutorial.exerciseMulti(
      s"""
          |Please map the input objects to the output objects, you'll want to change behaviour based on the value in ${grey("target")}
       """.stripMargin,
      List(
        json"""{"target": "a", "a": "hello"}""" -> json"""{"result": "hello"}""",
        json"""{"target": "b", "b": "world"}""" -> json"""{"result": "world"}""",
      ),
      true
    )
    _ <- tutorial.section("Bonus Section 4b: Branching with Style")
    _ <- tutorial.content(
      s"""
         |IDML has a more advanced branching expression which might feel more comfortable for tasks like the previous exercise, it's called the pattern match.
         |
         |You could've solved the previous exercise with this mapping:
         |${grey(
           """result = match target
              || this == "a" => a
              || this == "b" => b
           """)}
         |
         |You start off by using the ${grey("match")} keyword to give an expression you're matching over the result of, in this case ${grey("target")}
         |
         |You then give a set of cases on new lines, each starting with a ${grey("|")}, then a predicate, then a ${grey("=>")} and the expression to evaluate if the predicate matches.
         |
         |This allows you to branch multiple ways, and has the convenience of setting the scope for your predicates.
       """.stripMargin
    )
    _ <- tutorial.pause
    _ <- tutorial.section("Bonus Exercise 4b: Branching with Style")
    _ <- tutorial.exerciseMulti(
      s"""
         |You are given a cat, you need to determine if it's a good cat.
         |
         |The rules are:
         |
         |* if the cat has no name, we can't judge it and it's state is ${yellow("unknown")}
         |* if the cat has a name and it enjoys naps, it's a ${yellow("good")} cat
         |* if the cat has a name and doesn't enjoy naps, it's ${yellow("possibly good")}
         |
       """.stripMargin,
      List(
        json"""{"cat": {"enjoys": ["naps"]}}""" -> json"""{"status": "unknown"}""",
        json"""{"cat": {"name": "terry", "enjoys": ["naps"]}}""" -> json"""{"status": "good"}""",
        json"""{"cat": {"name": "bob", "enjoys": ["tennis"]}}""" -> json"""{"status": "possibly good"}"""
      ),
      true
    )
  } yield ()

}
