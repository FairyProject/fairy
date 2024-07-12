package io.fairyproject.data;

import java.io.Serializable;
import java.util.Map;

public interface MetaRegistry<K extends Serializable> {

    MetaStorage provide(K id);

    MetaStorage get(K id);

    void remove(K id);

    void destroy();

    Map<K, MetaStorage> cache();

}
