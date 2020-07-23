---
layout: docsplus
title: Basic Usage
section: api
---

If you want to embed the library in your service, here's a short guide on how to use its internal API.

## Hello world

Here's a simple Java "hello world":

    import io.idml.*;
    import io.idml.geo.GeoFunctionResolver;
    import io.idml.jackson.IdmlJackson;
    import io.idml.lang.DocumentParseException;

    public class JavaExample {
        public static void main(String[] args) {
            // Initialize the runtime. You only need one of these per application
            Idml idml = IdmlBuilder.withFunctionsFromServiceLoader().build();
            // Make a JSON implementation so we can pick a backend
            IdmlJson idmlJson = new IdmlJackson(); // you could pass in an ObjectMapper if you wanted
            // To avoid reflection and service loaders you initialize like this
            Idml idml2 = IdmlBuilder.withDefaultStaticFunctions(idmlJson).withResolver(new GeoFunctionResolver(idmlJson)).build();

            try {
                // Parse a simple mapping that turns
                Mapping mapping = idml.compile("x = a.b.int()");

                // Parse some json data taken from a string
                IdmlValue input = idmlJson.parse("{\"a\": {\"b\": \"123\"}}");

                // Run a mapping
                IdmlValue output = mapping.run(input);

                // Pretty-print the output as json
                System.out.println(idmlJson.pretty(output));
            } catch (DocumentParseException | IdmlJsonReadingException e) {
                e.printStackTrace();
            }
        }
    }


## Initialization

The Idml object is the main execution context of the system. It acts as a factory for mappings and can safely be shared between threads.

    Idml idml = IdmlBuilder.withFunctionsFromServiceLoader().build();

The IdmlJson object is used to manipulate the JSON representations, it is optional to use this, and you can use a `jackson` or `circe` based one.

    IdmlJson idmlJson = new IdmlJackson();

## Working with data

The IdmlValue class wraps some queryable data. Right now it supports JSON using Jackson or Circe and XML using JSoup.

    IdmlValue input = idmlJson.parse("{\"a\": {\"b\": \"123\"}}");

Basic transformations can be performed on the object itself, but usually this isn't necessary

    IdmlValue result = input.get("a").get("b").bool();

## Mappings

You can create a mapping from a string using the `compile` method, and it can throw a `DocumentParseException`:

    try {
        Mapping a = idml.compile("x = b");
    } catch (DocumentParseException e) {
        e.printStackTrace()
    }

## Chains

A chain is a way of composing mappings so that the results of one mapping are transformed into the next, allowing you to split your mapping up into smaller pieces.

This use case evolved from the interation data model at DataSift where we have raw data, for example, Twitter, a sanitized version of that data, and then generalized:

    try {
        Mapping facebookMapping = idml.chain(
             // Normalization: The text body of Facebook posts varies depending on whether it's a story, video, etc.
             idml.compile("facebook.content = this.(text | story | message).string()"),

             // Generalization: interaction.content refers to the body in all types.. Facebook, Twitter, Sina, etc.
             idml.compile("interaction.content = facebook.content.stripTags()"),

             // Validation: This schema validation is shared by all mappings to make sure that we follow uniform rules
             idml.compile("interaction.content : string().default(\"Aw damn, there's no tex on this one!\")")
        );
        // A simplified version of data received from the Facebook Graph API
        IdmlValue facebookPost = idmlJson.parse("{\"message\": \"Some message\"}");
        // Chains can be run just like any other mapping
        IdmlValue output = facebookMapping.run(facebookPost);
    } catch (DocumentParseException | IdmlJsonReadingException e) {
        e.printStackTrace();
    }

![chain diagram](./diagrams/chain.png)

## Mapping Merges

As an alternative to chains, merges allow you to run multiple mappings against the same input data, then merge the output in order.

This allows you to write parallel mappings rather than mappings which are applied end to end.


    try {
        // Merge some mappings together into one mapping
        Mapping merged = IdmlMapping.fromMultipleMappings(Arrays.asList(
                idml.compile("interaction.content = text"),
                idml.compile("interaction.author = author"),
                idml.compile("interaction.content = real_text")
        ));

        IdmlValue input = idmlJson.parse(
                "{\"text\": \"an example body\", \"author\": \"bob\", \"real_text\": \"an alternate body\"}"
        );

        System.out.println(idmlJson.pretty(merged.run(input)));
    } catch (DocumentParseException | IdmlJsonReadingException e) {
        e.printStackTrace();
    }

![chain diagram](./diagrams/merge.png)
