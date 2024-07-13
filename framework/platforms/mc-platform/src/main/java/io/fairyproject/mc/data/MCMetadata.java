package io.fairyproject.mc.data;

import io.fairyproject.data.MetaRegistry;
import io.fairyproject.data.MetaStorage;
import io.fairyproject.data.impl.MetaRegistryImpl;
import io.fairyproject.mc.MCEntity;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.MCWorld;
import io.fairyproject.mc.util.BlockPosition;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MCMetadata {

    private static final MetaRegistry<UUID> PLAYERS = new MetaRegistryImpl<>();
    private static final MetaRegistry<String> WORLDS = new MetaRegistryImpl<>();
    private static final MetaRegistry<UUID> ENTITIES = new MetaRegistryImpl<>();
    private static final MetaRegistry<Long> BLOCKS = new MetaRegistryImpl<>();

    public static MetaRegistry<UUID> getPlayerRegistry() {
        return PLAYERS;
    }

    public static MetaRegistry<String> getWorldRegistry() {
        return WORLDS;
    }

    public static MetaRegistry<UUID> getEntityRegistry() {
        return ENTITIES;
    }

    public static MetaRegistry<Long> getBlockRegistry() {
        return BLOCKS;
    }

    public static MetaStorage providePlayer(MCPlayer player) {
        return PLAYERS.provide(player.getUUID());
    }

    public static MetaStorage providePlayer(UUID player) {
        return PLAYERS.provide(player);
    }

    public static MetaStorage provideWorld(MCWorld world) {
        return WORLDS.provide(world.getName());
    }

    public static MetaStorage provideWorld(String world) {
        return WORLDS.provide(world);
    }

    public static MetaStorage provideEntity(MCEntity entity) {
        return ENTITIES.provide(entity.getUUID());
    }

    public static MetaStorage provideEntity(UUID entity) {
        return ENTITIES.provide(entity);
    }

    public static MetaStorage provideBlock(BlockPosition block) {
        return BLOCKS.provide(block.asLong());
    }

    @NotNull
    public static MetaStorage provide(Object holder) {
        if (holder instanceof MCPlayer) {
            return providePlayer((MCPlayer) holder);
        } else if (holder instanceof MCWorld) {
            return provideWorld((MCWorld) holder);
        } else if (holder instanceof MCEntity) {
            return provideEntity((MCEntity) holder);
        } else if (holder instanceof BlockPosition) {
            return provideBlock((BlockPosition) holder);
        }
        return null;
    }

    public static void clear() {
        PLAYERS.clear();
        WORLDS.clear();
        ENTITIES.clear();
        BLOCKS.clear();
    }

}
