---
layout: docsplus
title: URL
section: language
---

The url function allows the parsing and transformation of URLs

	 output = input.url()

## url
The simplest usage is to validate a URL

	output = input.url()

	{"input": "http://www.google.com/?q=apple"}
	{"output": "http://www.google.com/?q=apple"}

A variety of different protocols are accepted

	{"input": "https://www.google.com/?q=apple"}
	{"output": "https://www.google.com/?q=apple"}

	{"input": "ftp://www.google.com/"}
	{"output": "ftp://www.google.com/"}

	{"input": "mail://www.google.com/"}
	{"output": "mail://www.google.com/"}

### Fail to parse irregular urls and unsupported types...

	 {"input": "www.google.com/?q=apple"}
	 {}

	 {"input": false}
	 {}

	 {"input": 1234}
	 {}

	 {"input": null}
	 {}

	 {"input": "abc"}
	 {}

	 {"input": {"y": "Abc"}}
	 {}

### URL magic methods..


	    protocol = x.url().protocol
	    host     = x.url().host
	    path     = x.url().path
	    query    = x.url().query


### Parse regular urls...

 	{"x": "http://www.google.com/abc?q=apple"}

	{
	    "protocol": "http",
	    "host": "www.google.com",
	    "path": "/abc",
	    "query": "q=apple"
	}


	{"x": "https://www.google.com/abc?q=apple"}

	{
	 "protocol": "https",
	 "host": "www.google.com",
	 "path": "/abc",
	 "query": "q=apple"
	}

	{"x": "ftp://www.google.com/abc?q=apple"}

	{
	    "protocol": "ftp",
	    "host": "www.google.com",
	    "path": "/abc"
	    "query": "q=apple"
	}

### Fail to parse irregular urls and unsupported types..

	 {"x": "www.google.com/?q=apple"}   {}
	 {"x": false}   {}
	 {"x": 1234}    {}
	 {"x": null}    {}
	 {"x": "abc"}   {}
	 {"x": {"y": "Abc"}}   {}

## urls

Extracts links from a block of text.

	output = input.urls()

	{"input": "here's a link: https://idml.io. and here's a second link: http://idml.io/docs/getting-started-html"}

	{"output": ["https://idml.io", "http://idml.io/docs/getting-started.html"]}
