---
layout: docs
title: Whirlwind Tour
---

# Whirlwind Tour

## Assignments

Mappings consist of a left and right side separated with a single equality sign.

    my.output.field = some.input.field

The clear benefit of this is it handles object hierarchies pretty well. For example:

* If `my` or `output` don't exist then the nested structure is generated
* You wont get null-pointer exceptions if the right-hand side is missing. The field is simply missing from output unless you assign it to some other value or set a default

## Literals

If you want to assign a literal value to a field you can write something like this:

	my.text = "twitter"
	my.int = 123
	my.float = 123.4
	my.bool = true

## Coalesce

If we coalesce paths we iterate through a list of options and pick the first one we discover. For example, if we want to use _email.sender_ if _email.from_ isn't present we can do this:

    my.sender = email.(from | sender).name

## Wildcard
The wildcard symbol, _*_, can be used to match any field in an object.

    ex1 = *.b
    ex2 = a.*.b

## Anywhere wildcard
The anywhere wildcard symbol, _**_, can be used to match a sub-path anywhere inside an object.

    ex1 = **.a
    ex2 = a.**.b

## Functions
Any dynamic path can have one or more functions chained together. Some functions take the field they are called on whereas others require additional parameters.

    my.id      = any.number.int().max(100)
    my.string  = something.string().required()

### Built-in functions

Here's a list of available functions:

* `string.uppercase()`

The uppercase function converts the case of a string

    Input:  {"a": "hi there"}
    IDML:   x = a.uppercase()
    Output: {"x": "HI THERE"}


* `string.lowercase()`

The lowercase function converts the case of a string

    Input:  {"a": "Hi There!"}
    IDML:   x = a.lowercase()
    Output: {"x": "hi there!"}


* `pattern.format()`

The format function allows you to format values and concatenate strings with format-compatible format strings.

    Input:  {"a": 0.00097281289}
    IDML:   x = "%.2g".format(a)
    Output: {"x": "0.00097"}

for other formatting examples, see the [string module](../modules/string.md)

* `match(pattern)`, `split(pattern)`, `replace(search, replace)`, `isMatch(pattern)`,

see the [regex module](../modules/regex.md)

* `seed.random()`

`seed` may be a String, Int, or Object, and this will always output a 64-bit Int


    Input:  {"a": "1", "b": "2", "c": {"d": "3", "e": "4"}}
    IDML:   myrand   = c.d.random()
    Output: {"myrand": -8079212482812021964}


* `seed.random(min,max)`

`seed` may be a String, Int, or Object. `min` and `max` may be both Int or both Double


    Input:  {"a": "1", "b": "2", "c": {"d": "3", "e": "4"}}

    IDML:   rand_int   = c.random(0, 100)
            rand_float = c.random(0.0, 100.0)

    Output: {"rand_int": 68, "rand_float": 26.99476451853262}


* `object.serialize()`

Serializes this object into a JSON string

    Input:  {"c": {"d": "3", "e": "4", "f": 5}}

    IDML:   [main]
            summary = c.apply("summary_tpl").serialize()

            [summary_tpl]
            fld1 = d
            fld2 = e

    Output: {"summary": "{\"fld2\":\"4\",\"fld1\":\"3\"}"}


* `array.combinations(n)`

generates all the combinations of `n` elements from the items in the array

    Input:  {"mylist": ["a", "b", "c", "d"]}

    IDML:   combos2 = mylist.combinations(2)
            combos3 = mylist.combinations(3)

    Output: {
                "combos2": [ [ "a", "b" ], [ "a", "c" ], [ "a", "d" ], [ "b", "c" ], [ "b", "d" ], [ "c", "d" ] ],
                "combos3": [ [ "a", "b", "c" ], [ "a", "b", "d" ], [ "a", "c", "d" ], [ "b", "c", "d" ] ]
            }

* `array.empty()`

returns a bool saying if the input list is empty or not

    Input:  {"a": [], "b": ["not empty"]}

    IDML:   x = a.empty()
            y = b.empty()

    Output: {"x": true, "y" : false}


* `array.unique()`

filters the array down to unique items

    Input:  {"mylist": ["a", "b", "a", "c", "a", ""]}

    IDML:   x = mylist.unique()

    Output: {"x": ["a", "b", "c", ""]}

* `empty()`

returns true if the value is empty or null

    Input:  {"a": null, "b": "", c: false, d: "not empty"}

    IDML:   a1 = a.empty()
            b1 = b.empty()
            c1 = c.empty()
            d1 = d.empty()
            z1 = e.empty()

    Output: {"b1" : true,  "d1" : false}

this function can be very useful as a filter:

    Input:  {
                "entities": [
                    {
                        "name": "Bill Clinton",
                        "type": "Person"
                    },
                    {
                        "name": "Liza Minnelli"
                    },
                    {
                        "name": "New York New York song",
                        "type": ""
                    },
                    {
                        "name": "New York",
                        "type": "Location"
                    }
                ]
            }

    IDML:   names = entities [ type exists and not type.empty() == true].extract(name)

    Output: {"names": ["Bill Clinton", "New York"]}

* `isTrue`, `isFalse`, `isNull`, `isNothing`, `isString`, `isInt`

type checking functions

    Input:  {"a": true, "b": false, "c": null, "d": "xxxx", "e": 123}

    IDML:   a1 = a.isTrue()
            b1 = b.isFalse()
            c1 = c.isNull()
            d1 = d.isString()
            e1 = e.isInt()
            f1 = f.isNothing()

            a2 = a.isFalse()
            b2 = b.isTrue()
            c2 = c.isNothing()
            d2 = d.isInt()
            e2 = e.isString()

    Output: {
              "a1": true,
              "b1": true,
              "c1": true,
              "d1": true,
              "e1": true,
              "f1": true,
              "a2": false,
              "b2": false,
              "c2": false,
              "d2": false,
              "e2": false
            }


* `date().rssDate()`

formats the date in the RSS format ([RFC-822, 1036, 1123, 2822](https://tools.ietf.org/html/rfc5322#section-3.3))

    Input:  {"ts": 1518828435}
    IDML:   mydate = ts.date().rssDate()
    Output: {"mydate": "Sat, 17 Feb 2018 00:47:15 +0000"}

for other date functions, see the [date module](../modules/date.md)

* `email()`

parses an input field as email, and extracts its components

    Input:  {"x": "Bill Clinton <bill@whitehouse.gov>"}

    IDML:   addr   = x.email().address
            name   = x.email().name
            user   = x.email().username
            domain = x.email().domain

    Output: {
              "addr": "bill@whitehouse.gov",
              "name": "Bill Clinton",
              "user": "bill",
              "domain": "whitehouse.gov"
            }

* `url()`

parses an input field as a URL, and extracts its components

    Input:  {"x": "https://en.wikipedia.org/wiki/Andrea_Palladio?sections=all#Palaces"}

    IDML:   proto = x.url().protocol
            host  = x.url().host
            path  = x.url().path
            query = x.url().query

    Output: {
              "proto" : "https",
              "host" : "en.wikipedia.org",
              "path" : "/wiki/Andrea_Palladio",
              "query" : "sections=all"
            }

## Indexes
Array elements can be referred to with a numerical index.

The indexes begin at zero and a negative number will wrap around, so the first item can be retrieved with _0_ and the final item can be retrieved with _-1_

    first = a[0]
    last = a[-1]

## Variables
Variables allow you to refer to something that's been created on the left-hand side:

    blog.body = html
    blog.text = @blog.body.stripTags()

## Slices
A range of array elements can be sliced with two numerical values.

In the below examples we demonstrate how you can select the first four items, select everything except the first item and select the second, third and fourth item.

    first_four         = input[:3]
    tail               = input[1:]
    two_three_and_four = input[1:3]

## Filters
Filters are a custom form of CSDL that determines whether a mapping rule is applied or not.

    twitter.type        = "retweet" [ retweeted_status exists ]
    interaction.content = status [ type == "status" ].message

See them as an if statement; nothing will happen if the expression evaluates to false.

The customised form of CSDL can accept static paths and relative paths. The relative paths point to the item being iterated, which is particularly useful when filtering arrays.

     people_in_paris = people[location == "Paris"].extract(name)

NB: the left-hand-side provides scope for the square brackets, so given this input:

    {"a": "1", "b": "2", "c": {"d": "3", "e": "4"}}

this is the expected behaviour:

    x1 = [b == "2"] "test"      # invalid, as the conditional is missing a LHS, so the predicate will be ignored and by pure luck (aka undefined behaviour) will generate this result: {"x1": "test"}
    x2 = "test" [b == "2"]      # invalid, as the LHS is a literal value, without a child called "b"
    x3 = "test" [root.b == "2"] # use keyword `root` to break out of the current scope; result: {"x3": "test"}
    x4 = c [d == "3"].e         # result: {"x4": "4"}
    x5 = c [this.d == "3"]      # use keyword `this` to reference object in scope; result: {"x5": {"d": "3", "e": "4"}}
    x6 = c.e [root.a == "1"]    # use keyword `root` to break out of the current scope; result: {"x6": "4"}

### Filter field operators

    ex1 = my.field [my.other.field exists]
    ex2 = my.field [my.field == my.other.field]
    ex3 = my.field [my.field != my.other.field]

### Expression operators

The standard CSDL logical operators can be used. Operator precedence is currently undefined but can be enforced by using brackets.

    people_in_paris_named_jon     = people [location == "Paris" and name == "Jon" ].name
    people_in_paris_or_london     = people [location == "Paris" or location == "London" ].name
    people_not_in_paris_or_london = people [not (location == "Paris" or location == "London") ].name

Mapping rules are delimited by newlines but as long as the line ends with a dot you can continue on to the next line. CSDL filters are not newline sensitive as they begin and end with curly brackets.

## Blocks
In the following example,  _[main]_ designates the entry point of the mapping.

    [main]
    twitter            = [not retweeted_status exists].apply("tweet")
    twitter.retweet    = [retweeted_status exists].apply("tweet")
    twitter.retweeted  = retweeted_status.apply("tweet")

    [tweet]
    id                 = id_str
    created_at         = created_at.date()
    geo                = geo.coordinates.geo()
    place              = place.apply("place")

    [place]
    id 		           = id_str
    url 	           = place_link

If you want to use blocks then the mapping must have a _[main]_ block which acts as the entry point of hte application. Other templates can be activated with the _apply_ method, which becomes particularly powerful when paired with a CSDL filter.

### Scope
Inside the body of a template:

* The left-hand side is points to a new object
* The right-hand side is points to the object the template is applied to

The above makes templates very effective for re-using key bits of functionality.

So in the earlier example:

* __main__ iterates the data being submitted and returns the interaction
* __tweet__ iterates either the top level objects or their _retweeted_status_ and returns the retweet and _retweeted_ objects

You can use the __root__ and __this__ keyword if you want to be explicit about scope.


