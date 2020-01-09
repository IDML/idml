---
layout: docsplus
title: Predicates
section: language
---

Predicates are one of the two basic building blocks of IDML, they're boolean expressions which are used for ifs, matches and filters.

## Unitary Predicates

### exists

You use this by writing an expression and putting the keyword `exists` after it, if the expression resolves to a non-null value this returns true.

```
cat.name exists
```

### not

This takes any other predicate as it's right hand side, and negates it

```
not a > b
```

## Binary Expression Predicates

These take a normal expression as their left and right sides, with the operator in the middle

### substr

checks if the string on the left contains the string on the right

```
"hello" substr "lo"  # check if the string "hello" contains "lo
```

### ==
checks if the left side equals the right side

```
foo == 2
```

### !=
checks if the left side is not equal to the right side

```
foo != 2
```

### `<`, `<=`, `>` and `>=`

Your normal comparison operators which are used to compare numbers

```
a < b
4 >= a
a.b.c < a.b.d
```
### in

Checks if an array contains an item

```
1 in [1,2,3]
```

### contains

Checks if an array contains an item, like `in` but swapped order

```
[1,2,3] contains 1
```

## Binary Predicate Predicates

These take predicates as their left and right, with the operator in the middle

### and

Performs a binary and on the predicates

```
a > b and foo exists
```

### or

Performs a binary or on the predicates

```
a < b or bar exists
```

``
