package io.fairyproject.mc.meta;

import io.fairyproject.data.MetaRegistry;
import io.fairyproject.data.impl.MetaRegistryImpl;

import java.util.UUID;

public class MCMetaRegistries {

    private static final MetaRegistry<UUID> PLAYERS = new MetaRegistryImpl<>();
    private static final MetaRegistry<String> WORLDS = new MetaRegistryImpl<>();
    private static final MetaRegistry<UUID> ENTITIES = new MetaRegistryImpl<>();
    private static final MetaRegistry<Long> BLOCKS = new MetaRegistryImpl<>();



}
