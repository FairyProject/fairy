package io.fairyproject.container.util;

import io.fairyproject.container.type.TypeDescriptor;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Utility class for handling generic types in the container system.
 */
@UtilityClass
public class GenericTypeUtils {
    /**
     * Get a TypeDescriptor from a field, including generic information.
     *
     * @param field The field to extract type information from
     * @return A TypeDescriptor representing the field's type
     */
    public static TypeDescriptor getTypeDescriptorFromField(Field field) {
        Type genericType = field.getGenericType();
        return TypeDescriptor.fromType(genericType);
    }
    
    /**
     * Get a TypeDescriptor from a method parameter, including generic information.
     *
     * @param parameter The parameter to extract type information from
     * @return A TypeDescriptor representing the parameter's type
     */
    public static TypeDescriptor getTypeDescriptorFromParameter(Parameter parameter) {
        Type genericType = parameter.getParameterizedType();
        return TypeDescriptor.fromType(genericType);
    }
    
    /**
     * Check if a type is a parameterized type (has generic parameters).
     *
     * @param type The type to check
     * @return true if the type is parameterized
     */
    public static boolean isParameterizedType(Type type) {
        return type instanceof ParameterizedType;
    }
} 