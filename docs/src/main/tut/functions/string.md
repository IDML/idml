---
layout: docsplus
title: String
section: language
---

Functions for manipulating strings.

## string

Can be called on non-string things to turn them into strings

```
result = (2 + 2).string() # returns the string "4"
```

## uppercase, lowercase and capitalize

Used for manipulating the casing of strings

```
a1 = a.uppercase()   # uppercase all of the string
a2 = a.lowercase()   # lowercase all of the string
a3 = a.capitalize()  # capitalize the first letter of each word in the string
```

## strip

Strip whitespace from the beginning and end of the string.

```
output = input.strip()
{"input": "  hello world \n\t"}
{"output": "hello world"}
```

## sha1

Calculates the sha1 hash of the string

```
hashed = input.sha1()
```

## format

This is your normal format string application function, it's called on a string and takes any number of arguments

### Insert a simple string

     output = input.format(arg1)
     {"input": "%s", "arg1": "hello, world!"}
     {"output": "hello, world!"}

### Join two strings

     output = input.format(arg1, arg2)
     {"input": "%s %s", "arg1": "hello,", "arg2": "world!" }
     {"output": "hello, world!"}

### Pad numbers

     output = input.format(arg1)
     {"input": "%10d", "arg1": 42 }
     {"output": "        42"}

### Supply a lot of arguments

     output = input.format(a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z)
     {"input": "%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s", "a" :"a", "b" :"b", "c" :"c", "d" :"d", "e" :"e", "f" :"f", "g" :"g", "h" :"h", "i" :"i", "j" :"j", "k" :"k", "l" :"l", "m" :"m", "n" :"n", "o" :"o", "p" :"p", "q" :"q", "r" :"r", "s" :"s", "t" :"t", "u" :"u", "v" :"v", "w" :"w", "x" :"x", "y" :"y", "z" :"z" }
     {"output": "abcdefghijklmnopqrstuvwxyz"}

### Truncate a string

     output = input.format(arg1)
     {"input": "%.3s", "arg1": "hello" }
     {"output": "hel"}

### Format a double

     output = input.format(arg1)
     {"input": "%.2f", "arg1": 3.142 }
     {"output": "3.14"}

### Convert integer to hex

     output = input.format(arg1)
     {"input": "%#08x", "arg1": 7 }
     {"output": "0x000007"}

### Truncate a double by significant figures

     output = input.format(arg1)
     {"input": "%.2g", "arg1": 0.00097281289}
     {"output": "0.00097"}

### Covert a double to scientific notation

     output = input.format(arg1)
     {"input": "%e", "arg1": 0.00097281289}
     {"output": "9.728129e-04"}

### Right align a string

     output = input.format(arg1)
     {"input": "%5s", "arg1": "abc"}
     {"output": "  abc"}

### Put a number into a string

     output = input.format(arg1)
     {"input": "my number is %d", "arg1": 42}
     {"output": "my number is 42"}

### Format using a string literal

     output = "%s - %d".format(s, d)
     {"s": "mystring", "d": 12}
     {"output": "mystring - 12"}

### Combine values from literals only

     output = "%s - %d".format("foo", 71)
     {}
     {"output": "foo - 71"}

### Combine values from two object properties, for all objects in a list

    output = input.extract("%s|%s".format(type, name))
    {"input": [{"name": "a", "type": "A"}, {"name": "b", "type": "B"}]}
    {"output": ["A|a", "B|b"]}
