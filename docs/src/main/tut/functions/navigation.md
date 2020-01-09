---
layout: docsplus
title: Navigation
section: language
---

Functions for traversal and manipulation of arrays and objects.

## get

Get is the underlying function used to access fields by index or field name.

Get accepts a string or a number and can be used to retrieve an item in an object or array.

	x = a.get(0)
	y = b.get("c")

### literal numbers

	{"a": [123, 456]}

	x = a.get(1)
	y = a[1]

	{"x": 456, "y": 456}

### literal strings

	{"a": 123, "b": 456}

	x = get("b")
	y = b

	{"x": 456, "y": 456}


### dynamic loading

The main difference between get and using an array accessor or path is that the value can be dynamically loaded based on other input fields, for example:

	x = a.get(b)

	{"b": 1, "a": [123, 456]}
	{"x": 456}

Or when using a string:

	{"b": "my_field", "a": {"other_field": 123, "my_field": 456}}

	x = a.get(b)

	{"x": 456}


## deleted

This allows you to delete part of an object by setting it to the special call `deleted()`

```
{"a": {"good": 1, "bad": 2}}

output = a
output.bad = deleted()

{"output":{"good":1}}
```

## indexOf

This function can be used to find the index of a particular field in an array.

	{"input": ["a", "b", "c"]}

	result = input.indexOf("a")

	{"result": 0}

## slice

Slice is the underlying function called by the slice syntax.

### slice the first item

     b = a[0:1]

     {"a": [10, 11]}
     {"b": [10]}

     {"a": []}
     {"b": []}

     {}
     {}

### slice the first and second item

```
b = a[0:2]

{"a": [10, 11, 12]}
{"b": [10, 11]}

{"a": [10, 11]}
{"b": [10, 11]}

{"a": []}
{"b": []}
```

### compatibility check with JavaScript's slice(): a[1:2] == [a[1]]

```
b = a[1:2]
c = a[1]
d = a[1:1]


{"a": [10, 11, 12]}
{"b": [11], "c": 11, "d": []}

{"a": [10]}
{"b": [], "d": []}

{"a": []}
{"b": [], "d": []}
```

### slice second and third item

```
b = a[1:3]

{"a": [10, 11, 12, 13]}
{"b": [11, 12]}

{"a": [10, 11, 12]}
{"b": [11, 12]}

{"a": [10]}
{"b": []}

{"a": []}
{"b": []}
```

### slice first three items

```
b = a[:3]

{"a": [10, 11, 12, 13]}
{"b": [10, 11, 12]}

{"a": [10, 11]}
{"b": [10, 11]}

{"a": []}
{"b": []}
```

### slice everything after third

```
b = a[2:]

{"a": [10, 11, 12, 13]}
{"b": [12, 13]}

{"a": [10, 11]}
{"b": []}

{"a": []}
{"b": []}
```


