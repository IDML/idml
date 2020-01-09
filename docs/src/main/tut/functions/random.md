---
layout: docsplus
title: Random
section: language
---

The Random function is used to generate random numbers.


## random

The random function *takes a seed* and a range within which to generate the random number, remember to use a seed which makes sense from your input object [so that replayed data gets the same random number](../user-guide/philosophy.html#determinism).

The seed may be:
* a string, this is murmurhashed and fed in
* an integer, this is used to seed directly
* an object, this is serialized and murmurhashed

### generating a random number on the whole input
```
sample  = root.random()           # generates a random 64-bit integer seeded by the entire input object
sample1 = root.random(0, 100)     # generates a random integer between 0 and 100, seeded by the entire input
sample2 = root.random(0.0, 100.0) # generates a random float between 0.0 and 100.0, seeded by the entire input
```

### seeding with a couple fields
```
sample = (url + title).random(0, 100) # seed with two strings and range between 0 and 100
```
