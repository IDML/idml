---
layout: docsplus
title: Hashing 
section: language
---

The hashing module includes common hashing algorithms which may be called on strings

This module is optional and must be included in the list of FunctionResolvers passed to the Ptolemy engine constructor.


### Included hashing algorithms

| Algorithm  | Function Call  | Output |
|---|---|---|
| [xxHash32](https://cyan4973.github.io/xxHash/)  | `input.xxHash32()`  | 32-bit hex |
| [xxHash64](https://cyan4973.github.io/xxHash/)  | `input.xxHash64()`  | 64-bit hex |
| [cityHash](https://github.com/google/cityhash)  | `input.cityHash()`  | 64-bit hex |
| [murmurHash3](https://github.com/aappleby/smhasher)  | `input.murmurHash3()`  | 64-bit hex |
| [sha1](https://en.wikipedia.org/wiki/SHA-1)  | `input.sha1()` | 20-bit hex  |
| [sha256](https://en.wikipedia.org/wiki/SHA-2)  | `input.sha256()` | 256-bit hex |
| [sha512](https://en.wikipedia.org/wiki/SHA-2)  | `input.sha512()`  | 512-bit hex  |
| [md5](https://en.wikipedia.org/wiki/MD5)  | `input.md5()`  | 128-bit hex  |
| [crc32](https://en.wikipedia.org/wiki/Cyclic_redundancy_check)  | `input.crc32()`  | 32-bit hex |

Note: you can use the functions available on strings in the `Maths` set of functions to parse these into integers if you really need to, they are [parseHex](/functions/maths.html#parsehex) and [parseHexUnsigned](/functions/maths.html#parsehexunsigned), you probably want the latter.
