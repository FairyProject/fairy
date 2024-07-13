package io.fairyproject.data;

import java.io.Serializable;
import java.util.Map;

/**
 * A registry for MetaStorage
 *
 * @param <K> the type of the id
 */
public interface MetaRegistry<K extends Serializable> {

    /**
     * Provide a MetaStorage for the given id
     *
     * @param id the id
     * @return the MetaStorage
     */
    MetaStorage provide(K id);

    /**
     * Get the MetaStorage for the given id
     *
     * @param id the id
     * @return the MetaStorage
     */
    MetaStorage get(K id);

    /**
     * Remove the MetaStorage for the given id
     *
     * @param id the id
     */
    void remove(K id);

    /**
     * Clear the registry
     */
    void clear();

    /**
     * Get the cache of the registry
     *
     * @return the cache
     */
    Map<K, MetaStorage> cache();

}
