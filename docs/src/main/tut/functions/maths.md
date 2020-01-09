---
layout: docsplus
title: Maths
section: language
---

## Addition

### Adding a number to input

```
output = input + 2
{"input": 2}
{"output": 4}
```

### Adding together two literals

     output = 2 + 2
     {}
     {"output": 4}

### Adding together three literals

     output = 2 + 2 + 2
     {}
     {"output": 6}

### Adding strings together

```
output = "hello" + "world"
{}
{"output": "helloworld"}
```

## Multiplication
### Multiply input by a number

     output = input * 3
     {"input": 6}
     {"output": 18}

### Multiply two literals together

     output = 7 * 2
     {}
     {"output": 14}

### Multiply lots of literals together

     output = 9 * 8 * 7 * 6 * 5 * 4 * 3 * 2 * 1
     {}
     {"output": 362880}

## Division
### Divide input by a number

     output = input / 2
     {"input": 6}
     {"output": 3.0}

### Divide uneven input by a number

     output = input / 2
     {"input": 7}
     {"output": 3.5}

### Divide by zero

     output = input / 0
     {"input": 2}
     {}

## Subtraction
### subtract number from input

     output = input - 72
     {"input": 72}
     {"output": 0}

### subtract input from number

     output = 72 - input
     {"input": 72}
     {"output": 0}

### Subtract two doubles

     output = 3.142 - 2.71828
     {}
     {"output": 0.4237199999999999}

### subtract two doubles that give an int

     output = 3.5 - 2.5
     {}
     {"output": 1.0}

## Rounding

### floor

     output = 3.9.floor()
     {}
     {"output": 3}

     output = 1.23456.floor(2)
     {}
     {"output": 1.23}

### ceil

     output = 3.1.ceil()
     {}
     {"output": 4}

     output = 1.23456.ceil(2)
     {}
     {"output": 1.24}

### sigfig

     output = 123456.78.sigfig(3)
     {}
     {"output": 123000.0}

### parseHex

This parses a hex string as a signed 32-bit or 64-bit int depending on the length and will output an int if it's successful.

    output = "f0000000".parseHex()
    {}
    {"output": -268435456}

### parseHexUnsigned

This parses a hex string as an unsigned 32-bit of 64-bit int depending on the length, and will output a string if it's successful.

    output = "f0000000".parseHex()
    {}
    {"output": "4026531840"}
