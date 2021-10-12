package org.fairy.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Depend {

    /**
     * @return the module to depend on
     */
    String value();

    /**
     * @return the state for depend
     */
    State state() default State.HARD;

    enum State {

        SOFT,

        HARD

    }

}
