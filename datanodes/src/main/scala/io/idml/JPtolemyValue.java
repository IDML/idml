package io.idml;

import io.idml.datanodes.*;

import java.util.Map;

/**
 * The java-friendly API for constructing PtolemyValues
 *
 * You'll find `of` methods which allow you to construct the AST types from java
 *
 * The corresponding `asX` methods are on PtolemyValue itself, and return Optionals
 */
public class JPtolemyValue {
    public static PtolemyValue of(int i) {
        return new PInt(i);
    }
    public static PtolemyValue of(double d) {
        return new PDouble(d);
    }
    public static PtolemyValue of(long l) {
        return new PInt(l);
    }
    public static PtolemyValue of(boolean b) {
        return new PBool(b);
    }
    public static PtolemyValue of(PtolemyValue... v) {
        return PArray.of(v);
    }
    public static PtolemyValue of(Map<String, PtolemyValue> kv) {
        return PObject.of(kv);
    }
}
