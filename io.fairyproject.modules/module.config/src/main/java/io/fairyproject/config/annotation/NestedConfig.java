package io.fairyproject.config.annotation;

import java.lang.annotation.*;
import java.lang.annotation.ElementType;

/**
 * This annotation will mark the configuration class to scan field in super classes that is inside {@link NestedConfig#value()}
 * So it opens up the possibility to have multi layers of config options
 *
 * @since 0.0.1b1-SNAPSHOT
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NestedConfig {

    Class<?>[] value();

}
