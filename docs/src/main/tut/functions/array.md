---
layout: docsplus
title: Array
section: language
---

Functions for manipulating arrays

## wrapArray

Wraps a single item in an array, this may be replaced by array literals at some point.

```
2.wrapArray() # returns [2]
```

## empty

Asks an array or string if it's empty, returns a boolean

## unique

Filters an array down to unique items


```
{"input": [1,1,2,2,3,3]}

output = input.unique()

{"output": [1,2,3]}

```

## unique(expression)

This is the advanced version of unique which takes a key expression, anything with a null/nothing key will be dropped.

The first item with a given key will be kept, and ordering will be preserved.

```
{"input": ["a", "A", "B", "b"]}

output = input.unique(this.uppercase())

{"output": ["a", "B"]}
```

## combinations

Return all combinations of items in a list, takes an integer of combination length.

```
{"input": [1,2,3]}

output = input.combinations(2)

{"output": [[1,2], [1,3], [2,3]]}
```

## sort

Sort the array in ascending order, this will also sort the array by type first.

```
{"input": [3,2,1]}

output = input.sort()

{"output": [1,2,3]}
```

## sort(expression)

This is the advanced version of sort which takes a key expression, anything with a null/nothing key will be dropped.

```
{"input": [5, 4, "1", null, {}]}

output = input.sort(this.int())

{"output": ["1", 4, 5]}
```

## flatten

This flattens an array by one level, bringing any arrays in it up into the top level array.

```
{"input": [[1,2,3],4,[[5,6,7]]]}

output = input.flatten()

{"output" : [1,2,3,4,[5,6,7]]}
```

## combineAll

This takes an array which is all one type, and combines all the items into one item.

This is implemented for array, object, string, int and double.

| type | combine method |
|---|---|
| array | append |
| object | merge |
| string | concat |
| int | addition |
| double | addition |

```
{"input": ["hello", "world"]}

output = input.combineAll()

{"output" : "helloworld"}
```

```
{"input": [{"a": 1}, {"b": 2}, {"a": 3}]}

output = input.combineAll()

{"output": {"b": 2, "a": 3}}
```

## zip

This takes an array and another array, and zips them together, throwing away the tail of the longest array.

```
{}

output = [1,2,3,4,5].zip(["a", "b", "c"])

{"result": [[1, "a"], [2, "b"], [3, "c"]]}
```
