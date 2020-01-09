---
layout: docsplus
title: Advanced
section: language
---

Advanced functions which resolve differently to normal functions.

## apply and applyArray

Invokes a [Block](features/core-language.html#blocks) on an item or array of items.

## array and extract

These both do what you'd call `flatMap` in functional languages, they're used to transform items in an array and will drop items which resolve to Nothing.

```
{"input": [1, 2, 3, 4]}

output = input.extract(this + 1)

{"output" : [ 2, 3, 4, 5 ]}
```

## blacklist

Removes a blacklisted field from an object.

```
{"input": {"a": 1, "b": 2, "c": 3}}

output = input.blacklist("b")

{"output" : {"a" : 1, "c" : 3 }}
```

This would be the same as

```
output = input
output.b = deleted()
```

## average

Averages an array of ints and doubles, always returns a double.

```
{"input": [1,2,3,4,5,6,7,8,9]}

output = input.average()

{"output" : 5.0}
```

## append

Appends an item to a list.

## prepend

Prepends an item to a list.

## size

Gets the size of an array or string when called with no arguments, trims it down to a size when called with an integer.

## concat

Joins two strings together, mostly replaced by `+`.

