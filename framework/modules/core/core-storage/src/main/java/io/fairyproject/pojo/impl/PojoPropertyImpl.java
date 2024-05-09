package io.fairyproject.pojo.impl;

import io.fairyproject.ObjectSerializer;
import io.fairyproject.container.Autowired;
import io.fairyproject.container.SerializerFactory;
import io.fairyproject.metadata.MetadataMap;
import io.fairyproject.mysql.pojo.CustomSerialize;
import io.fairyproject.pojo.PojoProperty;
import io.fairyproject.util.AccessUtil;
import io.fairyproject.util.exceptionally.ThrowingRunnable;
import io.fairyproject.util.exceptionally.ThrowingSupplier;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PojoPropertyImpl implements PojoProperty {

    @Autowired
    private static SerializerFactory SERIALIZER_FACTORY;

    private final String name;
    private final Class<?> instanceType;
    private final Class<?> type;
    private final ObjectSerializer<?, ?> serializer;
    private final Field field;
    private final Method readMethod;
    private final Method writeMethod;
    private final MetadataMap metadataMap;

    public PojoPropertyImpl(Class<?> instanceType, Field field) {
        this.instanceType = instanceType;
        this.field = field;
        this.name = field.getName();
        this.type = field.getType();

        this.readMethod = this.findReadMethod();
        this.writeMethod = this.findWriteMethod();
        this.serializer = this.findSerializer();
        this.metadataMap = MetadataMap.create();
    }

    private ObjectSerializer<?, ?> findSerializer() {
        CustomSerialize customSerialize = this.field.getDeclaredAnnotation(CustomSerialize.class);
        if (customSerialize != null) {
            return SERIALIZER_FACTORY.findOrCacheSerializer(customSerialize.value());
        }

        return SERIALIZER_FACTORY.findSerializer(this.type);
    }

    private Method findWriteMethod() {
        Class<?> type = this.instanceType;

        final String byName = fieldMethodName("set", name);
        final String byFieldName = fieldMethodName("set", field.getName());

        Method retVal = findMethod(type, byName, byFieldName);
        if (retVal != null) {
            ThrowingRunnable.sneaky(() -> AccessUtil.setAccessible(retVal)).run();
        }
        return retVal;
    }

    private Method findMethod(Class<?> type, String byName, String byFieldName) {
        Class<?> trimType = type;
        while (trimType != Object.class) {
            for (Method method : trimType.getDeclaredMethods()) {
                if (!method.getName().equals(byName) && !method.getName().equals(byFieldName))
                    continue;
                final Class<?>[] parameters = method.getParameterTypes();
                if (parameters.length != 1)
                    continue;
                if (this.type.isAssignableFrom(parameters[0]))
                    return method;
            }
            trimType = trimType.getSuperclass();
        }
        return null;
    }

    private Method findReadMethod() {
        Class<?> type = this.instanceType;

        final String byName = fieldMethodName("get", name);
        final String byFieldName = fieldMethodName("get", field.getName());

        Method retVal = null;

        while (type != Object.class) {
            try {
                Method method = type.getDeclaredMethod(byName);
                if (this.type.isAssignableFrom(method.getReturnType())) {
                    retVal = method;
                    break;
                }

                method = type.getDeclaredMethod(byFieldName);
                if (this.type.isAssignableFrom(method.getReturnType())) {
                    retVal = method;
                    break;
                }
            } catch (NoSuchMethodException ignored) {}
            type = type.getSuperclass();
        }

        if (retVal != null) {
            Method finalRetVal = retVal;
            ThrowingRunnable.sneaky(() -> AccessUtil.setAccessible(finalRetVal)).run();
        }
        return retVal;
    }

    private String fieldMethodName(String prefix, String fieldName) {
        String firstCharacter = fieldName.toCharArray()[0] + "".toUpperCase();
        return prefix + firstCharacter + fieldName.substring(1);
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Field field() {
        return this.field;
    }

    @Override
    public Object get(Object instance) {
        return ThrowingSupplier.sneaky(() -> {
            if (this.readMethod != null) {
                return this.readMethod.invoke(instance);
            }
            return this.field.get(instance);
        }).get();
    }

    @Override
    public void set(Object instance, Object obj) {
        if (!this.type().isInstance(obj)) {
            return;
        }
        ThrowingRunnable.sneaky(() -> {
            if (this.writeMethod != null) {
                this.writeMethod.invoke(instance, obj);
                return;
            }
            this.field.set(instance, obj);
        }).run();
    }

    @Override
    public ObjectSerializer<?, ?> serializer() {
        return this.serializer;
    }

    @Override
    public Class<?> type() {
        return this.type;
    }

    @Override
    public MetadataMap getMetadataMap() {
        return this.metadataMap;
    }
}
