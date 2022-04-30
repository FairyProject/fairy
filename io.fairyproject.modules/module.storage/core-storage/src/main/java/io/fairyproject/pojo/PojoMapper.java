package io.fairyproject.pojo;

import com.google.gson.JsonObject;
import io.fairyproject.metadata.MetadataMapProxy;
import io.fairyproject.pojo.impl.PojoMapperImpl;
import io.fairyproject.pojo.listener.PojoListenerPrimaryKey;

import java.util.Map;
import java.util.function.Consumer;

public interface PojoMapper<T> extends MetadataMapProxy {

    static <T> PojoMapper<T> create(Class<T> type) {
        return new PojoMapperImpl<>(type);
    }

    static <T> PojoMapper<T> createDatabase(Class<T> type) {
        return create(type)
                .addListener(new PojoListenerPrimaryKey());
    }

    void init() throws ReflectiveOperationException;

    Map<String, PojoProperty> properties();

    default PojoProperty getProperty(String name) {
        return properties().getOrDefault(name, null);
    }

    PojoMapper<T> addListener(PojoListener pojoListener);

    void callListener(Consumer<PojoListener> consumer);

}
