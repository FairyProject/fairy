package io.fairyproject.data;

import java.lang.ref.Reference;
import java.util.concurrent.atomic.AtomicInteger;

public interface MetaKey<T> {

    AtomicInteger ID_COUNTER = new AtomicInteger(0);

    int getId();

    String getName();

    Class<T> getType();

    default T cast(Object value) {
        if (value == null)
            return null;

        if (value instanceof Reference) {
            Object obj = ((Reference<?>) value).get();
            if (obj == null)
                return null;

            return getType().cast(obj);
        }

        return getType().cast(value);
    }

    static int getCurrentCapacity() {
        return ID_COUNTER.get() + 1;
    }

}
