---
layout: docsplus
title: jsoup (HTML and XML)
section: language
---

This module is optional and must be included in the list of FunctionResolvers passed to the Ptolemy engine constructor.

It includes the `PtolemyJsoup` class which can be used to parse XML or HTML into input which can be read by the interpreter.

## PtolemyJsoup

When coding against this, you can use `PtolemyJsoup.parseXml` and `PtolemyJsoup.parseHtml` to load data.

## stripTags

This module also includes the `stripTags()` function, which can be called on a string to strip all XML and HTML tags out of it.

```
"<b>hello</b> world, this has some <i>tags</i> in".stripTags()     # "hello world, this has some tags in"
```

