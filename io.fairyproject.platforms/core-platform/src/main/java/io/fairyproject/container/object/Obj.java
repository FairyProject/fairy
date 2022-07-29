package io.fairyproject.container.object;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The client of the fairy dependency injection ecosystem
 * An {@link Obj} supports constructor injection and field injection.
 * All the life cycle are handled internally in fairy.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Obj {
}
