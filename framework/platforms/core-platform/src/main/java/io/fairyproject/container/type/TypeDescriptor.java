package io.fairyproject.container.type;

import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

/**
 * Describes a type including its raw class and generic type parameters.
 * Used to support generic type matching in dependency injection.
 */
@Data
public class TypeDescriptor {
    private final Class<?> rawType;
    private final Type[] genericTypes;

    public TypeDescriptor(@NotNull Class<?> rawType) {
        this(rawType, null);
    }

    public TypeDescriptor(@NotNull Class<?> rawType, @Nullable Type[] genericTypes) {
        this.rawType = rawType;
        this.genericTypes = genericTypes;
    }

    /**
     * Create a TypeDescriptor from a Type object.
     *
     * @param type The type to create from
     * @return A new TypeDescriptor
     */
    public static TypeDescriptor fromType(Type type) {
        if (type instanceof Class<?>) {
            return new TypeDescriptor((Class<?>) type);
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class<?>) {
                return new TypeDescriptor((Class<?>) rawType, parameterizedType.getActualTypeArguments());
            }
        }
        throw new IllegalArgumentException("Unsupported type: " + type);
    }

    /**
     * Check if this type is assignable to the target type.
     * Considers both raw type compatibility and generic type parameters.
     *
     * @param target The target type descriptor
     * @return true if this type is assignable to the target type
     */
    public boolean isAssignableTo(TypeDescriptor target) {
        // Check raw type compatibility
        if (!target.getRawType().isAssignableFrom(this.rawType)) {
            return false;
        }

        // If target has no generic parameters, it's compatible
        if (target.getGenericTypes() == null || target.getGenericTypes().length == 0) {
            return true;
        }

        // If this type has no generic info but target does, not compatible
        if (this.genericTypes == null || this.genericTypes.length == 0) {
            return false;
        }

        // Generic parameter count must match
        if (this.genericTypes.length != target.getGenericTypes().length) {
            return false;
        }

        // Check each generic parameter
        for (int i = 0; i < this.genericTypes.length; i++) {
            Type thisType = this.genericTypes[i];
            Type targetType = target.getGenericTypes()[i];
            
            // For Class types, check assignability
            if (targetType instanceof Class<?> && thisType instanceof Class<?> && !((Class<?>) targetType).isAssignableFrom((Class<?>) thisType)) {
                return false;
            }
        }
        
        return true;
    }
} 