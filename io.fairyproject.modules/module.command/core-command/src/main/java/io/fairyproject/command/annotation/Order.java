package io.fairyproject.command.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation to mark the Order of sub command to display in Help
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Order {

    /**
     * The order to display in help command
     *
     * @return lower display first
     */
    int value();

}
