---
layout: docsplus
title: UUID
section: language
---

The uuid functions allow the generation of UUIDs.

Design Note: Type 1, 2 and 4 are not included because they are not deterministic, [see Philosophy](/philosophy.html#determinism).

## uuid3

Generates version 3 UUIDs with MD5.

This function takes a string or object, and when called with no arguments will run against the current root object.

```
{"input": "hello world"}

idml> output = input.uuid3()

{"output": "5eb63bbb-e01e-3ed0-93cb-22bb8f5acdc3"}
```

### hash the entire input object

```
{}

hash1 = root.uuid3()
hash2 = uuid3()
# as long as you're not in a block these are equivalent

{
  "hash2" : "99914b93-2bd3-3a50-b983-c5e7c90ae93b",
  "hash1" : "99914b93-2bd3-3a50-b983-c5e7c90ae93b"
}
```

## uuid5

Generates version 5 UUIDs with SHA-1.

This function takes a string or object, and when called with no arguments will run against the current root object.

```
{"input": "hello world"}

idml> output = input.uuid5()

{"output": "2aae6c35-c94f-5fb4-95db-e95f408b9ce9"}
```

### hash the entire input object

```
{}

hash1 = root.uuid5()
hash2 = uuid5()
# as long as you're not in a block these are equivalent

{
  "hash2" : "bf21a9e8-fbc5-5384-afb0-5b4fa0859e09",
  "hash1" : "bf21a9e8-fbc5-5384-afb0-5b4fa0859e09"
}
```


