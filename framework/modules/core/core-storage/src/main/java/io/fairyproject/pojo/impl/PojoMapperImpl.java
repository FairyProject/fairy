package io.fairyproject.pojo.impl;

import io.fairyproject.metadata.MetadataMap;
import io.fairyproject.pojo.PojoListener;
import io.fairyproject.pojo.PojoMapper;
import io.fairyproject.pojo.PojoOrder;
import io.fairyproject.pojo.PojoProperty;
import io.fairyproject.util.AccessUtil;
import io.fairyproject.util.Utility;

import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;

public class PojoMapperImpl<T> implements PojoMapper<T> {

    private final Map<String, PojoProperty> properties = new LinkedHashMap<>();
    private final MetadataMap metadataMap = MetadataMap.create();
    private final List<PojoListener> listeners = new ArrayList<>();
    private final Class<T> type;

    public PojoMapperImpl(Class<T> type) {
        this.type = type;
    }

    @Override
    public void init() throws ReflectiveOperationException {
        final int modifiers = type.getModifiers();
        if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers)) {
            throw new IllegalArgumentException("POJOMapperImpl doesn't support abstract/interface classes!");
        }

        List<PojoProperty> properties = new ArrayList<>();
        for (Field field : Utility.getAllFields(this.type)) {
            final int fieldModifiers = field.getModifiers();
            if (field.isAnnotationPresent(Transient.class) ||
                    Modifier.isStatic(fieldModifiers) ||
                    Modifier.isTransient(fieldModifiers)) {
                continue;
            }

            if (Modifier.isFinal(fieldModifiers)) {
                throw new IllegalArgumentException("POJOMapperImpl doesn't support field with final modifier! " + field);
            }

            AccessUtil.setAccessible(field);

            final PojoProperty property = PojoProperty.create(this.type, field);
            properties.add(property);

            this.callListener(pojoListener -> pojoListener.onPropertyAdded(this, property));
        }

        properties.stream()
                .sorted(Comparator.comparingInt(property -> {
                    final PojoOrder columnOrder = property.field().getAnnotation(PojoOrder.class);
                    if (columnOrder != null) {
                        return columnOrder.value();
                    }
                    return 0;
                }))
                .forEachOrdered(pojoProperty -> this.properties.put(pojoProperty.name(), pojoProperty));

        this.callListener(pojoListener -> pojoListener.onMapperInitialized(this));
    }

    @Override
    public Map<String, PojoProperty> properties() {
        return this.properties;
    }

    @Override
    public PojoMapper<T> addListener(PojoListener pojoListener) {
        this.listeners.add(pojoListener);
        return this;
    }

    @Override
    public void callListener(Consumer<PojoListener> consumer) {
        this.listeners.forEach(consumer);
    }

    @Override
    public MetadataMap getMetadataMap() {
        return this.metadataMap;
    }
}
