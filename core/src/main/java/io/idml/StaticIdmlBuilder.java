package io.idml;

import io.idml.functions.FunctionResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StaticIdmlBuilder extends IdmlBuilder {
    List<FunctionResolver> resolvers = new ArrayList<FunctionResolver>();

    public StaticIdmlBuilder() {
        super(Optional.empty());
        List<FunctionResolver> resolvers = new ArrayList<>();
        this.functionResolver = Optional.of(new StaticFunctionResolverService(resolvers));
        this.resolvers = resolvers;
    }

    public StaticIdmlBuilder(IdmlJson json) {
        super(Optional.empty());
        resolvers.addAll(StaticFunctionResolverService.defaults(json));
        this.functionResolver = Optional.of(new StaticFunctionResolverService(resolvers));
    }

    public StaticIdmlBuilder withResolver(FunctionResolver resolver) {
        resolvers.add(resolver);
        this.functionResolver = Optional.of(new StaticFunctionResolverService(resolvers));
        return this;
    }

    public StaticIdmlBuilder withResolverPrepend(FunctionResolver resolver) {
        resolvers.add(0, resolver);
        this.functionResolver = Optional.of(new StaticFunctionResolverService(resolvers));
        return this;
    }

}
