package io.fairyproject.container;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Suggest annotation in IDEA
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface AnnotateSuggest {

    String value();

}
