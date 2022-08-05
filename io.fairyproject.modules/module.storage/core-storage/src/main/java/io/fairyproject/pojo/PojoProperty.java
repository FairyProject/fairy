package io.fairyproject.pojo;

import io.fairyproject.ObjectSerializer;
import io.fairyproject.metadata.MetadataMapProxy;
import io.fairyproject.pojo.impl.PojoPropertyImpl;

import java.lang.reflect.Field;

public interface PojoProperty extends MetadataMapProxy {

    static PojoProperty create(Class<?> instanceType, Field field) {
        return new PojoPropertyImpl(instanceType, field);
    }

    String name();

    Field field();

    Object get(Object instance);

    void set(Object instance, Object obj);

    ObjectSerializer<?, ?> serializer();

    Class<?> type();

}
