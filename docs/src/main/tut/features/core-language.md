---
layout: docsplus
title: Core Language
section: language
---

## Assignments

These consist of a path into the output object, an equals sign, and an expression of what to put in that path.

    my.output.field = some.input.field

The clear benefit of this is it handles object hierarchies pretty well. For example:

* If `my` or `output` don't exist then the nested structure is generated
* You wont get null-pointer exceptions if the right-hand side is missing. The field is simply missing from output unless you assign it to some other value or set a default

## Temporary Variables

These are used to store expressions which are used while processing the item, they use the keyword `let` to find a variable which is not insert into the final output.

```idml
let DATEFORMAT = "yyyy-MM-dd"
date = created_at.date($DATEFORMAT)
```

## Expressions

Expressions are sections of code which can evaluate, these are covered in depth in the [Expressions](expressions.html) section.

## Predicates

Predicates are sections of code which can evaluate to a true or false, and are used for control flow. These are covered in depth in the [Predicates](predicates.html) section.

## Blocks

These are used to separate sections of a mapping, if there are none, the entire document is put inside a `[main]` block automatically, if you use blocks you must designate your own `main` block.

```idml
[main]
twitter            = root.apply("tweet")

[tweet]
id                 = id_str
```

You can apply blocks with the `apply()` function as shown in the example.

### Scope
Inside the body of a template:

* The left-hand side is points to a new object
* The right-hand side is points to the object the template is applied to

The above makes templates very effective for re-using key bits of functionality.

So in the earlier example:

* __main__ iterates the data being submitted and returns the interaction
* __tweet__ iterates either the top level objects or their _retweeted_status_ and returns the retweet and _retweeted_ objects

You can use the __root__ and __this__ keyword if you want to be explicit about scope.


