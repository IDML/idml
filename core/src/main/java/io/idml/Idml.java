package io.idml;

import io.idml.datanodes.IObject;
import io.idml.functions.FunctionResolver;
import io.idml.lang.DocumentParseException;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * The main IDML class; can be used to compile IDML, merge Mappings and run them
 */
public class Idml {
    private IdmlParser parser;
    private FunctionResolverService functionResolver;
    List<IdmlListener> listeners;

    Idml(
            IdmlParser parser,
            FunctionResolverService functionResolver,
            List<IdmlListener> listeners
    ) {
        this.parser = parser;
        this.functionResolver = functionResolver;
        this.listeners = listeners;
    }

    /**
     * Compile some IDML into a Mapping
     * @param in
     * @return a compiled Mapping
     * @throws DocumentParseException when the document is invalid
     */
    public Mapping compile(String in) throws DocumentParseException {
        return parser.parse(functionResolver, in);
    }

    /**
     * Create a Chain of mappings end to end
     * @param chain
     * @return
     */
    public Mapping chain(Mapping... chain) {
        return IdmlChain.of(Arrays.asList(chain));
    }

    /**
     * Creates a set of mappings that are run in parallel and have their outputs merged
     * @param chain
     * @return
     */
    public Mapping merge(Mapping... chain) {
        return IdmlMapping.fromMultipleMappings(Arrays.asList(chain));
    }

    /**
     * Run a given mapping with some input, returning the output
     * @param mapping
     * @param input
     * @return the output of running the program
     */
    public IdmlObject run(Mapping mapping, IdmlObject input) {
        IdmlContext ctx = new IdmlContext(input, IdmlJson.newObject());
        ctx.setListeners(listeners);
        return mapping.run(ctx).output();
    }

    /***
     * Evaluate a given mapping with some input, returning the whole computed Context
     *
     * You probably only need this method if you want to access the listener state bags
     * @param mapping
     * @param input
     * @return
     */
    public IdmlContext evaluate(Mapping mapping, IdmlObject input) {
        IdmlContext ctx = new IdmlContext(input, IdmlJson.newObject());
        ctx.setListeners(listeners);
        return mapping.run(ctx);
    }

    /**
     * Evaluate a given mappen in a given Context, returning the computed Context
     *
     * You probably only need this method if you want to access the listener state bags
     * @param mapping
     * @param ctx
     * @return
     */
    public IdmlContext evaluate(Mapping mapping, IdmlContext ctx) {
        ctx.setListeners(listeners);
        return mapping.run(ctx);
    }


    public static AutoIdmlBuilder autoBuilder()  {
        return new AutoIdmlBuilder();
    }

    public static StaticIdmlBuilder staticBuilder() {
        return new StaticIdmlBuilder();
    }

    public static StaticIdmlBuilder staticBuilderWithDefaults(IdmlJson json) {
        return new StaticIdmlBuilder(json);
    }

}
