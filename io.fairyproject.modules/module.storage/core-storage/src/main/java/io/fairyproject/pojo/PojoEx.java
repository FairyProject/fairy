package io.fairyproject.pojo;

import io.fairyproject.metadata.MetadataKey;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PojoEx {

    public final MetadataKey<PojoProperty> PRIMARY_KEY = MetadataKey.create("pojo:primary-key", PojoProperty.class);

}
