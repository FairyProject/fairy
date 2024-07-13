package io.fairyproject.mc.data;

import io.fairyproject.data.MetaStorage;
import org.jetbrains.annotations.NotNull;

public interface MCMetadataBridge {

    @NotNull MetaStorage provide(@NotNull Object object);

}
