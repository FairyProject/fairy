package io.fairyproject.util;

import lombok.Getter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * Represents a parameterized type. Unfortunately necessary because the JVM doesn't support
 * reified generics in all cases.
 */
@SuppressWarnings("unchecked")
@Getter
public class TypeLiteral<T> {

    /**
     * The type represented by this type token.
     */
    private final Class<T> type;

    /**
     * The type parameters to the type represented by this type token.
     */
    private final TypeLiteral<?>[] parameters;

    /**
     * Creates a new type token for the given type, parameterized with the given type parameters.
     */
    public TypeLiteral(Class<T> type, TypeLiteral<?>... parameters) {
        this.type = type;
        this.parameters = parameters;
    }

    /**
     * Attempts to create a type token by using the JVM's limited generic reification capabilities.
     * This constructor must be invoked from a subtype to work!
     */
    protected TypeLiteral() {
        Type thisType = getClass().getGenericSuperclass();
        try {
            Type tp = ((ParameterizedType) thisType).getActualTypeArguments()[0];
            if (tp instanceof ParameterizedType) {
                ParameterizedType ptp = (ParameterizedType) tp;
                this.type = (Class<T>) ptp.getRawType();
                this.parameters = Arrays.stream(ptp.getActualTypeArguments())
                        .map(TypeLiteral::new).toArray(TypeLiteral[]::new);
            } else {
                this.type = (Class<T>) tp;
                this.parameters = new TypeLiteral[0];
            }
        } catch (Exception e) {
            throw new UnsupportedOperationException("Could not compute parameterized type for " + thisType, e);
        }
    }

    /**
     * Attempts to create a type token from a reified generic type, assuming all its parameters are also reified.
     */
    private TypeLiteral(Type tp) {
        try {
            if (tp instanceof ParameterizedType) {
                ParameterizedType ptp = (ParameterizedType) tp;
                this.type = (Class<T>) ptp.getRawType();
                this.parameters = Arrays.stream(ptp.getActualTypeArguments())
                        .map(TypeLiteral::new).toArray(TypeLiteral[]::new);
            } else {
                this.type = (Class<T>) tp;
                this.parameters = new TypeLiteral[0];
            }
        } catch (Exception e) {
            throw new UnsupportedOperationException("Could not compute parameterized type for " + tp, e);
        }
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(type.getSimpleName());
        if (parameters.length > 0) {
            buf.append("<").append(parameters[0].toString());
            for (int i = 1; i < parameters.length; i++) {
                buf.append(", ").append(parameters[i].toString());
            }
            buf.append(">");
        }
        return buf.toString();
    }

}