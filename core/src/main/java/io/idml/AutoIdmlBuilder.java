package io.idml;

import java.util.Optional;

public class AutoIdmlBuilder extends IdmlBuilder {
    public AutoIdmlBuilder() {
        super(Optional.of(new FunctionResolverService()));
    }
}
