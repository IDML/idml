# DataNodes

This project is an alternative JSON DOM built on top of Jackson. The primary motivation for creating this, as opposed to using json4s or jackson-databind, is to provide richer and more specialised node types like PUrl and PCsv

## Usage

You can use the global methods on the DataNodes class to create a JSON DOM from a variety of sources:

	DataNodes.parse(in: String): PValue
	DataNodes.parse(in: Reader): PValue
	DataNodes.parse(in: InputStream): PValue
	DataNodes.parse(in: File): PValue
	
If you want to turn a JSON DOM back into a string you can use one of the following serialization mechanisms, depending on whether you want to create a pretty-printed or compact string:

	DataNodes.compact(d: PValue): String
	DataNodes.pretty(d: PValue): String

The parser is currently pretty leniant as it enables the following Jackson parser features:

* ALLOW_SINGLE_QUOTES enables strings like 'abc'
* ALLOW_COMMENTS allows both // and /\* \*/ style quotes
* ALLOW_UNQUOTED_FIELD_NAMES allows objects like {abc: def}

## PValue

PValue is the base class for all JSON DOM nodes. If you're familiar with Jackson or Json4 it's a crossover of JsonNode and JValue with added features tailored to our use case.

### Rich nodes

You might notice that we have classes named with "Like" on the end. The idea behind this is that we are able to create new versions of primitive data types that have extra functionality.

For example, the PURL class is a PStringLike which can sanitize, validate and render a single URL but also provide accessors to subcomponents in the URL:

	my.domain = link.url().domain


### Transform functions

Transforms are just methods that return new PValues. Consider the default implementation of the _string()_ method: 

	def string(): PValue = this match {
	  case _: PStringLike | _: PNothingLike => this
	  case n: PIntLike    => PValue(n.value.toString)
	  case n: PDoubleLike => PValue(n.value.toString)
	  case _ => PCastUnsupported
	}

### Exception-free navigation

In the _string()_ example you might have noticed the peculiar PCastUnsupported return value. The DOM should never throw exceptions but instead return objects that represent missing values. Here are some examples:

* _PMissingField_ will be returned instead of _null_ when we try to get a field from an object that doesn't exist
* _PFailedRequirement_ will be returned when a required field is missing
* _PNull_ is used when we explicitly want a null value to show up in the results

### Constrainted mutability

Immutable data structures allow us to write predictable pure functions but mutable data structures have lower CPU and memory requirements. 

* Primitives like int are immutable
* Complex structures like object and array can have their fields modified
* The deepCopy() function allows functions to create a safe local copy

The default implementation of _deepCopy()_ immutable structures can return themselves and complex structures can make a copy of themselves.

## Implementations of PValue

Primitive base classes:

* PStringLike
* PIntLike
* PBoolLike
* PDoubleLike
* PNothingLike

Complex base classes:

* PObjectLike
* PArrayLike

Example complex base classes:

* PUrl for working with urls
* PUUID for working with UUIDs
* PDate for working with dates
* PGeo for working with lat-long objects
* PCsv for working with comma-separated values

## Adding a new rich type

You'll need to do the following:

1. Pick a name: You'll need to use this in multiple places
1. Pick a base class: What is the fundamental type of data the class overlaps?
1. Write integration tests: Watch them burn
1. Create a behaviour class with new functions and default implementations
1. The PValue class mixes in the behaviour trait
1. Create the implementation class and override any existing behaviour functions

For example with our PUrl implementation:

1. PUrl extends StringLike
1. PUrlBehaviour adds url()
1. PValue extends PUrlBehaviour
1. PUrl overrides get() to support fields like _domain_, _protocol_ and _path_ even though it's not a JSON object


