/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.fairyproject.bukkit.metadata;

import io.fairyproject.bukkit.metadata.type.BlockMetadataRegistry;
import io.fairyproject.bukkit.metadata.type.EntityMetadataRegistry;
import io.fairyproject.bukkit.metadata.type.PlayerMetadataRegistry;
import io.fairyproject.bukkit.metadata.type.WorldMetadataRegistry;
import io.fairyproject.mc.util.BlockPosition;
import io.fairyproject.metadata.MetadataKey;
import io.fairyproject.metadata.MetadataMap;
import io.fairyproject.metadata.MetadataRegistry;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides access to {@link MetadataRegistry} instances bound to players, entities, blocks and worlds.
 */
public final class Metadata {

    private static BukkitMetadataRegistries registries;

    static BukkitMetadataRegistries getRegistries() {
        if (registries == null) {
            registries = new BukkitMetadataRegistries();
        }

        return registries;
    }

    public static void destroy() {
        registries = null;
    }

    /**
     * Gets the {@link MetadataRegistry} for {@link Player}s.
     *
     * @return the {@link PlayerMetadataRegistry}
     */
    public static PlayerMetadataRegistry players() {
        return getRegistries().getPlayerRegistry();
    }

    /**
     * Gets the {@link MetadataRegistry} for {@link Entity}s.
     *
     * @return the {@link EntityMetadataRegistry}
     */
    public static EntityMetadataRegistry entities() {
        return getRegistries().getEntityRegistry();
    }

    /**
     * Gets the {@link MetadataRegistry} for {@link Block}s.
     *
     * @return the {@link BlockMetadataRegistry}
     */
    public static BlockMetadataRegistry blocks() {
        return getRegistries().getBlockRegistry();
    }

    /**
     * Gets the {@link MetadataRegistry} for {@link World}s.
     *
     * @return the {@link WorldMetadataRegistry}
     */
    public static WorldMetadataRegistry worlds() {
        return getRegistries().getWorldRegistry();
    }

    /**
     * Produces a {@link MetadataMap} for the given object.
     *
     * A map will only be returned if the object is an instance of
     * {@link Player}, {@link UUID}, {@link Entity}, {@link Block} or {@link World}.
     *
     * @param obj the object
     * @return a metadata map
     */
    @Nonnull
    public static MetadataMap provide(@Nonnull Object obj) {
        Objects.requireNonNull(obj, "obj");
        if (obj instanceof Player) {
            return provideForPlayer(((Player) obj));
        } else if (obj instanceof UUID) {
            return provideForPlayer(((UUID) obj));
        } else if (obj instanceof Entity) {
            return provideForEntity(((Entity) obj));
        } else if (obj instanceof Block) {
            return provideForBlock(((Block) obj));
        } else if (obj instanceof World) {
            return provideForWorld(((World) obj));
        } else {
            throw new IllegalArgumentException("Unknown object type: " + obj.getClass());
        }
    }

    /**
     * Gets a {@link MetadataMap} for the given object, if one already exists and has
     * been cached in this registry.
     *
     * A map will only be returned if the object is an instance of
     * {@link Player}, {@link UUID}, {@link Entity}, {@link Block} or {@link World}.
     *
     * @param obj the object
     * @return a metadata map
     */
    @Nonnull
    public static Optional<MetadataMap> get(@Nonnull Object obj) {
        Objects.requireNonNull(obj, "obj");
        if (obj instanceof Player) {
            return getForPlayer(((Player) obj));
        } else if (obj instanceof UUID) {
            return getForPlayer(((UUID) obj));
        } else if (obj instanceof Entity) {
            return getForEntity(((Entity) obj));
        } else if (obj instanceof Block) {
            return getForBlock(((Block) obj));
        } else if (obj instanceof World) {
            return getForWorld(((World) obj));
        } else {
            throw new IllegalArgumentException("Unknown object type: " + obj.getClass());
        }
    }

    @Nonnull
    public static MetadataMap provideForPlayer(@Nonnull UUID uuid) {
        return players().provide(uuid);
    }

    @Nonnull
    public static MetadataMap provideForPlayer(@Nonnull Player player) {
        return players().provide(player);
    }

    @Nonnull
    public static Optional<MetadataMap> getForPlayer(@Nonnull UUID uuid) {
        return players().get(uuid);
    }

    @Nonnull
    public static Optional<MetadataMap> getForPlayer(@Nonnull Player player) {
        return players().get(player);
    }

    @Nonnull
    public static <T> Map<Player, T> lookupPlayersWithKey(@Nonnull MetadataKey<T> key) {
        return players().getAllWithKey(key);
    }

    @Nonnull
    public static MetadataMap provideForEntity(@Nonnull UUID uuid) {
        return entities().provide(uuid);
    }

    @Nonnull
    public static MetadataMap provideForEntity(@Nonnull Entity entity) {
        return entities().provide(entity);
    }

    @Nonnull
    public static Optional<MetadataMap> getForEntity(@Nonnull UUID uuid) {
        return entities().get(uuid);
    }

    @Nonnull
    public static Optional<MetadataMap> getForEntity(@Nonnull Entity entity) {
        return entities().get(entity);
    }

    @Nonnull
    public static <T> Map<Entity, T> lookupEntitiesWithKey(@Nonnull MetadataKey<T> key) {
        return entities().getAllWithKey(key);
    }

    @Nonnull
    public static MetadataMap provideForBlock(@Nonnull BlockPosition block) {
        return blocks().provide(block);
    }

    @Nonnull
    public static MetadataMap provideForBlock(@Nonnull Block block) {
        return blocks().provide(block);
    }

    @Nonnull
    public static Optional<MetadataMap> getForBlock(@Nonnull BlockPosition block) {
        return blocks().get(block);
    }

    @Nonnull
    public static Optional<MetadataMap> getForBlock(@Nonnull Block block) {
        return blocks().get(block);
    }

    @Nonnull
    public static <T> Map<BlockPosition, T> lookupBlocksWithKey(@Nonnull MetadataKey<T> key) {
        return blocks().getAllWithKey(key);
    }

    @Nonnull
    public static MetadataMap provideForWorld(@Nonnull UUID uid) {
        return worlds().provide(uid);
    }

    @Nonnull
    public static MetadataMap provideForWorld(@Nonnull World world) {
        return worlds().provide(world);
    }

    @Nonnull
    public static Optional<MetadataMap> getForWorld(@Nonnull UUID uid) {
        return worlds().get(uid);
    }

    @Nonnull
    public static Optional<MetadataMap> getForWorld(@Nonnull World world) {
        return worlds().get(world);
    }

    @Nonnull
    public static <T> Map<World, T> lookupWorldsWithKey(@Nonnull MetadataKey<T> key) {
        return worlds().getAllWithKey(key);
    }

    private Metadata() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
