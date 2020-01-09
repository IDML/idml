---
layout: docsplus
title: Regex
section: language
---

These functions allow you to manipulate strings using Java regular expressions.


## match
### match with no groups

     output = input.match("a")
     {"input": "a"}
     {"output": []}

### match with no groups with regex style argument

     output = input.match("a")
     {"input": "a"}
     {"output": []}

### match with no groups

     output = input.match("(a+)")
     {"input": "aaa"}
     {"output": ["aaa"]}

### match a load of groups

     output = input.match("(foo) (bar) (baz)")
     {"input": "foo bar baz"}
     {"output": ["foo", "bar", "baz"]}


## matches
### extract multiple matches with groups from a string
    
    output = input.matches("(a)")
    {"input": "aaa"}
    {"output": [["a"],["a"],["a"]]}

## Split
### split by a character

     output = input.split("a")
     {"input": "abababab"}
     {"output": ["", "b", "b", "b", "b"]}

### split by a regex

     output = input.split("(a|b)")
     {"input": "acbcab"}
     {"output": ["", "c", "c"]}

### split on whitespace

     output = input.split("[ \t\n]+")
     {"input": "foo bar   baz \n tabbed"}
     {"output": ["foo", "bar", "baz", "tabbed"]}

## Replace
### replace a letter

     output = input.replace("f", "z")
     {"input": "foo"}
     {"output": "zoo"}

### replace a word

     output = input.replace("bar", "zoo")
     {"input": "foo bar baz"}
     {"output": "foo zoo baz"}

### replace a lot of letters

     output = input.replace(".", "dog")
     {"input": "foo"}
     {"output": "dogdogdog"}

## isMatch
### one character match

     output = input.isMatch("a")
     {"input": "a"}
     {"output": true}


## multi character match

     output = input.isMatch("foo")
     {"input": "foo"}
     {"output": true}

### wildcard match

     output = input.isMatch(".*coo.*")
     {"input": "ca"}
     {"output": true}

