package io.fairyproject.gradle.util;

import lombok.experimental.UtilityClass;
import org.gradle.api.provider.Property;

import java.util.function.Consumer;

@UtilityClass
public class PropertyUtil {

    public <T> void ifPresent(Property<T> property, Consumer<T> consumer) {
        final T t = property.getOrNull();
        if (t != null) {
            consumer.accept(t);
        }
    }

}
