package io.fairyproject.command.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.METHOD})
public @interface Usage {

    /**
     * Return the usage message that will be displayed on help
     *
     * @return usage message
     */
    String value();

    /**
     * Overwrite the entire usage command
     * If false -> /<baseCommand> <args> - <usage>
     * If true -> <usage>
     *
     * @return true if overwrite
     */
    boolean overwrite() default false;

    /**
     * Only display this command on help if the user has the permission
     *
     * @return true if only display on having permission
     */
    boolean displayOnPermission() default false;

}
