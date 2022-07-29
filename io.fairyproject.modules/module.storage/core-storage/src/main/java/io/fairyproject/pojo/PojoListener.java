package io.fairyproject.pojo;

public interface PojoListener {

    void onPropertyAdded(PojoMapper<?> pojoMapper, PojoProperty pojoProperty);

    void onMapperInitialized(PojoMapper<?> pojoMapper);

}
