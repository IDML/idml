---
layout: docsplus
title: Geo
section: language
---

This module is optional and must be included in the list of FunctionResolvers passed to the Ptolemy engine constructor.

This module provides methods for working with geographical objects.

## geo

The geo function creates Geo objects

	 output = input.geo()

### Convert existing objects into geo objects

This function will operate on arrays and objects

	 {"input": [7.5, 1.0]}
	 {"output": {"latitude": 7.5, "longitude": 1.0}}

	 {"input": {"longitude": 7.5, "latitude": 2.0}}
	 {"output": {"longitude": 7.5, "latitude": 2.0}}

This function will cast other types to floats

	 {"input": ["7.5", 1]}
	 {"output": {"latitude": 7.5, "longitude": 1.0}}

	 {"input": {"longitude": "7.5", "latitude": "2.0"}}
	 {"output": {"longitude": 7.5, "latitude": 2.0}}

	The geo function creates Geo objects using arguments

### geo with arguments

The Geo function can create Geo objects with fields passed in as arguments

	 output = geo(input.a, input.b)

#### Apply the geo function to 2 arguments

This function will operate on arrays and objects

	 {"input": {"a": 4.5, "b": 12.0}}
	 {"output": {"latitude": 4.5, "longitude": 12.0}}

	 {"input": {"a": "4.5", "b": "12"}}
	 {"output": {"latitude": 4.5, "longitude": 12.0}}

#### It should cope with Reading's longitude and latitude: 51.4542° N, 0.9731° W

     {"input": {"a": 51.4542, "b": 0.9731}}
     {"output": {"latitude": 51.4542, "longitude": 0.9731}}


#### Fail gracefully when it's unsupported

     {"input": {"a": "dog", "b": "cat"}}
     {}

     {}
     {}

## country

This function resolves ISO country codes to their name

```
country("GB") # "United Kingdom"
```

## region

This function resolves ISO region codes to their name

```
region("GB", "N7") # "Surrey"
```

## admin1

This function resolves GeoNames Admin1 areas to their name

```
admin1(2638360) # {"name": "Scotland", "asciiname": "Scotland", "id": 2638360}
```

## city

This function resolves GeoNames Cities to their name

```
city(2639577) # {"name": "Reading", "asciiname": "Reading", "id": 2639577}
```
