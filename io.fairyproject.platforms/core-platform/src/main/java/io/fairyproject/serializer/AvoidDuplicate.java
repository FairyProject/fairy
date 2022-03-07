package io.fairyproject.serializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a {@link io.fairyproject.ObjectSerializer} class to avoid key type duplication.
 *
 * if duplication happens, it will always prioritize the first one that has been registered,
 * This annotation will strictly lock the serializer to 1 only each key.
 * Without this annotation, no error will be thrown but instead a warning will show up in console.
 * With this annotation, error will be thrown and potentially make the plugin not working.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AvoidDuplicate {
}
