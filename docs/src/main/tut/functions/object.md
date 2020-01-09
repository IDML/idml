---
layout: docsplus
title: Object
section: language
---

Object functions are used to manipulate and work with objects.

## serialize

This function is called on an Object and returns a String containing a JSON serialized copy of the object

```idml:input
{}
```

```idml:code:inline
a.b = 1
a.c = 2

serialized = @a.serialize()
```

## keys

This function is called on an object and returns an array of its keys.

## values

This function is called on an object and returns an array of its values.
