package io.idml;

import io.idml.functions.FunctionResolver;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Builder for creating IDML engines, should be created with either of the static methods.
 */
public class IdmlBuilder {
    private IdmlParser parser = new IdmlParser();
    protected Optional<FunctionResolverService> functionResolver = Optional.empty();
    private List<FunctionResolver> staticFunctionResolvers = new LinkedList<>();
    private List<IdmlListener> listeners = new LinkedList<IdmlListener>();

    public IdmlBuilder(Optional<FunctionResolverService> frs) {
        this.functionResolver = frs;
    };

    public IdmlBuilder(FunctionResolverService frs) {
        this.functionResolver = Optional.of(frs);
    }

    /**
     * Create an IDML builder with automatic function loading from the classpath
     * @return an auto-flavoured builder
     */
    public static AutoIdmlBuilder withFunctionsFromServiceLoader() {
        return new AutoIdmlBuilder();
    }

    /**
     * Create an IDML builder with static functions which will be registered with `withListener`
     * @return a static-flavoured builder
     */
    public static StaticIdmlBuilder withStaticFunctions() {
        return new StaticIdmlBuilder();
    }

    /**
     * Create an IDML builder with static functions, starting with the defaults, based on the given IdmlJson implementation
     * @param json the JSON engine to use for serialization/deserialization methods
     * @return a static-flavoured builder with the default methods pre-registered
     */
    public static StaticIdmlBuilder withDefaultStaticFunctions(IdmlJson json) {
        return new StaticIdmlBuilder(json);
    }

    /**
     * Register a listener for extra functionality, usually used for analysis
     * @param listener a listener to be activated on IDML visitor events
     * @return the modified builder
     */
    public IdmlBuilder withListener(IdmlListener listener) {
        listeners.add(listener);
        return this;
    }

    /**
     * Build into an IDML engine
     * @throws java.util.NoSuchElementException if you haven't picked a Function Resolver
     * @return the built IDML engine
     */
    public Idml build() {
        return new Idml(parser, functionResolver.get(), listeners);
    }

}
