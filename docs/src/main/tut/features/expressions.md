---
layout: docsplus
title: Expressions
section: language
---

Expressions are the main building block of IDML, they evaluate into values.

## Literals

If you want to assign a literal value to a field you can write something like this:

	my.text = "twitter"
	my.int = 123
	my.float = 123.4
	my.bool = true

## Coalesce

The Coalesce expression is a pair of rounded brackets, and expressions with bars between them, it evaluates as the first expression which wasn't null.

```idml
my.sender = email.(from | sender).name    # this will attempt to use email.from, and otherwise use email.sender
```


## If Expressions

These are used to branch on a predicate.

```idml
result = if a > b then c else d      # if a is bigger than b, set result to c, otherwise set it to d
result1 = if foo exists then bar     # if the variable foo exists, then set result1 to bar
```

## Pattern Matching

This lets you inspect a value with a list of predicates and expressions, and run the first one which matches.

```idml
result = match value
| this < 5  => "less than 5"
| this > 5  => "more than 5"
| this == 5 => "five"
| _ => "fallthrough"
```

Of course it's an expression so you can do something complex like this:

```idml
result = ("the answer is: " + match value
| this < 5 => "less than 5"
| this > 5 => "more than 5"
| this == 5 => "five"
).capitalize()
```

## Relative Paths

These are used to evaluate a path from the *input* within the current context, they can start with `this`, or just be a variable path.

```idml
this      # the current scope
this.a    # traverse into a inside the current scope
a         # as above
this.a.b  # get the b field from the a object in the current scope
a.b       # as above
this.*    # get every object inside the current scope
*         # as above
this.*.b  # get every b field inside every object in the current scope
*.b       # as above
```

## Absolute Paths

These have the prefix of `root` and also address the *input* object.

```idml
root.a    # get a from the root scope
root.a.b  # get b from a from the root scope
```

## Variable Paths

These are used to address the *output* object and have the `@` prefix

```idml
foo = "hello"
bar = @foo    # set the `bar` field to whatever we've already put in `foo`
```

## Temporary Variable Paths

These are used to address *temporary variables* and have the `$` prefix

```idml
let foo = "hello"
bar = $foo     # set the `bar` field to the expression stored in the temporary variable `foo`
```

## Array Traversal

Array elements can be referred to with a numerical index.

The indexes begin at zero and a negative number will wrap around, so the first item can be retrieved with _0_ and the final item can be retrieved with _-1_

```idml
first = a[0]
last = a[-1]
```

## Array Slices
A range of array elements can be sliced with two numerical values.

In the below examples we demonstrate how you can select the first four items, select everything except the first item and select the second, third and fourth item.

```idml
first_four         = input[:3]
tail               = input[1:]
two_three_and_four = input[1:3]
```

## Functions

Functions are either provided as a globally available function, or are available on certain data types as a suffix, see the next few sections for full listings.

```idml
my.id      = any.number.int().max(100)
my.string  = "%s %s".format(a, b).capitalize()
```


## Filters
Filters allow you to filter an array or object using a [predicate](/features/predicates.html):

```idml
twitter.type        = "retweet" [ retweeted_status exists ]
interaction.content = status [ type == "status" ].message
```

See them as an if statement; nothing will happen if the expression evaluates to false.

These are evaluated inside a relative scope of the item being traversed over, setting `this` to the current item, and evaluating all paths inside it, which is useful for filtering arrays:

```idml
people_in_paris = people[location == "Paris"].extract(name)
```

NB: the left-hand-side provides scope for the square brackets, so given this input:

```json
{"a": "1", "b": "2", "c": {"d": "3", "e": "4"}}
```

this is the expected behaviour:

```idml
x1 = [b == "2"] "test"      # invalid, as the conditional is missing a LHS, so the predicate will be ignored and by pure luck (aka undefined behaviour) will generate this result: {"x1": "test"}
x2 = "test" [b == "2"]      # invalid, as the LHS is a literal value, without a child called "b"
x3 = "test" [root.b == "2"] # use keyword `root` to break out of the current scope; result: {"x3": "test"}
x4 = c [d == "3"].e         # result: {"x4": "4"}
x5 = c [this.d == "3"]      # use keyword `this` to reference object in scope; result: {"x5": {"d": "3", "e": "4"}}
x6 = c.e [root.a == "1"]    # use keyword `root` to break out of the current scope; result: {"x6": "4"}
```

## Maths

The operators `+`, `-`, `/` and `*` are available for numeric operations, they take an expression as their left and right and resolve them.

```idml
a = 1 + 2
b = 2 / 3
c = d * e
```

See the Maths section for full documentation.
