package io.fairyproject.pojo.listener;

import io.fairyproject.pojo.PojoEx;
import io.fairyproject.pojo.PojoListener;
import io.fairyproject.pojo.PojoMapper;
import io.fairyproject.pojo.PojoProperty;
import io.fairyproject.util.ConditionUtils;

import javax.persistence.Id;

public class PojoListenerPrimaryKey implements PojoListener {
    @Override
    public void onPropertyAdded(PojoMapper<?> pojoMapper, PojoProperty pojoProperty) {
        if (pojoProperty.field().isAnnotationPresent(Id.class)) {
            pojoMapper.getOrPut(PojoEx.PRIMARY_KEY, () -> pojoProperty);
        }
    }

    @Override
    public void onMapperInitialized(PojoMapper<?> pojoMapper) {
        ConditionUtils.check(pojoMapper.has(PojoEx.PRIMARY_KEY), "The mapper doesn't contain any primary key! (field with @javax.persistence.Id annotation.)");
    }
}
