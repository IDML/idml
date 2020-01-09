---
layout: docsplus
title: Getting Started
section: language
---

## Reading these docs

When this documentation contains code examples, it'll have some JSON input, the code block, then some output, like so:

```idml:input
{"a": 2, "b": 2}
```
```idml:code
result = a + b
```

## REPL

The REPL is the simplest way to try things out. You can download it from [here](https://github.com/IDML/idml/releases) and then run it with:

	java -jar idml-1.0.xxx.jar repl

It'll give you a prompt asking for some input JSON, then ask for some IDML to run against it, for example:

```
json> {"a": "hello", "b": "world"}
....>
JSON accepted

idml> output = "%s %s".format(a, b)
....>
{
  "output" : "hello world"
}
```

## Tool

The [idml-tool](https://github.com/IDML/idml-tool) is the alternative way to evaluate IDML on the command line. It is invoked on a command line with a set of mappings and will take line-delimited JSON on stdin and map it to stdout:

### Usage

```
Ptolemy IDML command line tool.
Usage: idml apply [options] <file>...

  --help              Show usage information and flags
  --pretty <value>    Enable pretty printing of output
  --unmapped <value>  This probably doesn't do what you think it does
  <file>...           one or more mapping files to run the data through
```

### Example

```shell
andi@andi-workstation examples > cat > input.json
{"a": "hello", "b": "world"}
{"a": "hello"}
{}
andi@andi-workstation examples > cat > mapping.idml
output = "%s %s".format(a, b)
andi@andi-workstation examples > java -jar idml.jar apply mapping.idml < input.json
{"output":"hello world"}
{}
{}
```
