package io.idml;

import io.idml.datanodes.*;

import java.util.Map;

/**
 * The java-friendly API for constructing IdmlValues
 *
 * You'll find `of` methods which allow you to construct the AST types from java
 *
 * The corresponding `asX` methods are on IdmlValue itself, and return Optionals
 */
public class JIdmlValue {
    public static IdmlValue of(int i) {
        return new IInt(i);
    }
    public static IdmlValue of(double d) {
        return new IDouble(d);
    }
    public static IdmlValue of(long l) {
        return new IInt(l);
    }
    public static IdmlValue of(boolean b) {
        return new IBool(b);
    }
    public static IdmlValue of(IdmlValue... v) {
        return IArray.of(v);
    }
    public static IdmlValue of(Map<String, IdmlValue> kv) {
        return IObject.of(kv);
    }
}
