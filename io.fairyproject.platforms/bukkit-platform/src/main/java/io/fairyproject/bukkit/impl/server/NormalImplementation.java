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

package io.fairyproject.bukkit.impl.server;

import com.google.common.collect.HashMultimap;
import io.fairyproject.Fairy;
import io.fairyproject.bukkit.Imanity;
import io.fairyproject.bukkit.impl.annotation.ServerImpl;
import io.fairyproject.bukkit.metadata.Metadata;
import io.fairyproject.bukkit.player.movement.MovementListener;
import io.fairyproject.bukkit.player.movement.impl.AbstractMovementImplementation;
import io.fairyproject.bukkit.player.movement.impl.BukkitMovementImplementation;
import io.fairyproject.bukkit.reflection.MinecraftReflection;
import io.fairyproject.bukkit.reflection.resolver.ConstructorResolver;
import io.fairyproject.bukkit.reflection.resolver.MethodResolver;
import io.fairyproject.bukkit.reflection.resolver.ResolverQuery;
import io.fairyproject.bukkit.reflection.resolver.minecraft.NMSClassResolver;
import io.fairyproject.bukkit.reflection.resolver.minecraft.OBCClassResolver;
import io.fairyproject.bukkit.reflection.wrapper.*;
import io.fairyproject.bukkit.util.BlockPositionData;
import io.fairyproject.bukkit.util.CoordXZ;
import io.fairyproject.mc.util.BlockPosition;
import io.fairyproject.metadata.MetadataKey;
import io.fairyproject.task.Task;
import io.fairyproject.util.AccessUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * TODO
 * - Modern version compatibility
 */
@ServerImpl
public class NormalImplementation implements ServerImplementation {

    public static final MetadataKey<ConcurrentMap> FAKE_BLOCK_MAP = MetadataKey.create(Fairy.METADATA_PREFIX + "FakeBlockMap", ConcurrentMap.class);

    private static final ObjectWrapper MINECRAFT_SERVER;

    private static final Class<?> CHUNK_COORD_PAIR_TYPE;
    private static Class<?> BLOCK_INFO_TYPE;

    private static FieldWrapper<Float> BLOCK_SLIPPERINESS_FIELD;
    private static FieldWrapper GAME_PROFILE_FIELD;

    private static MethodWrapper<?> BLOCK_GET_BY_ID_METHOD;
    private static MethodWrapper<?> FROM_LEGACY_DATA_METHOD;
    private static MethodWrapper<?> GET_PROFILE_ENTITY_HUMAN_METHOD;
    private static final MethodWrapper<?> GET_ENTITY_BY_ID_METHOD;

    private static ConstructorWrapper<?> BLOCK_INFO_CONSTRUCTOR;
    private static final ConstructorWrapper<?> SPAWN_NAMED_ENTITY_CONSTRUCTOR;
    private static final ConstructorWrapper<?> CHUNK_COORD_PAIR_CONSTRUCTOR;
    private static final ConstructorWrapper<?> DESTROY_ENTITY_CONSTRUCTOR;

    static {
        final NMSClassResolver CLASS_RESOLVER = new NMSClassResolver();
        final OBCClassResolver OBC_RESOLVER = new OBCClassResolver();
        try {
            final Field field = OBC_RESOLVER.resolve("inventory.CraftMetaSkull").getDeclaredField("profile");
            AccessUtil.setAccessible(field);
            GAME_PROFILE_FIELD = new FieldWrapper<>(field);
            GET_PROFILE_ENTITY_HUMAN_METHOD = new MethodWrapper<>(CLASS_RESOLVER.resolve("world.entity.player.EntityHuman", "EntityHuman").getMethod("getProfile"));

            Class<?> minecraftServerType = CLASS_RESOLVER.resolve("server.MinecraftServer", "MinecraftServer");
            Object minecraftServer = minecraftServerType.getMethod("getServer").invoke(null);
            MINECRAFT_SERVER = new ObjectWrapper(minecraftServer);

            try {
                Class<?> BLOCK_INFO_PACKET_TYPE = CLASS_RESOLVER.resolve("network.protocol.game.PacketPlayOutMultiBlockChange", "PacketPlayOutMultiBlockChange");
                Class<?> BLOCK_INFO_TYPE;
                try {
                    BLOCK_INFO_TYPE = CLASS_RESOLVER.resolve("network.protocol.game.PacketPlayOutMultiBlockChange$MultiBlockChangeInfo", "PacketPlayOutMultiBlockChange$MultiBlockChangeInfo");
                } catch (ClassNotFoundException ex) {
                    BLOCK_INFO_TYPE = CLASS_RESOLVER.resolve("MultiBlockChangeInfo");
                }

                NormalImplementation.BLOCK_INFO_TYPE = BLOCK_INFO_TYPE;
                Class<?> blockData = CLASS_RESOLVER.resolve("world.level.block.state.IBlockData", "IBlockData");

                ConstructorResolver constructorResolver = new ConstructorResolver(BLOCK_INFO_TYPE);
                BLOCK_INFO_CONSTRUCTOR = new ConstructorWrapper<>(constructorResolver.resolve(
                        new Class[]{short.class, blockData},
                        new Class[]{BLOCK_INFO_PACKET_TYPE, short.class, blockData}
                ));
            } catch (Exception ex) {

                Imanity.LOGGER.error("Having trouble while looking up MultiBlockChange packet (1_16_R2 ?)");
                // v1_16_R2 changed it for no reason fuck off
                // PacketPlayOutMultiBlockChange(final SectionPosition sectionposition, final ShortSet shortset, final ChunkSection chunksection, final boolean flag)

            }

            Class<?> blockType = CLASS_RESOLVER.resolve("world.level.block.Block", "Block");
            try {
                BLOCK_GET_BY_ID_METHOD = new MethodWrapper<>(blockType.getMethod("getById", int.class));
                FROM_LEGACY_DATA_METHOD = new MethodWrapper<>(blockType.getMethod("fromLegacyData", int.class));
            } catch (Exception ex) {

                Imanity.LOGGER.error("The id of Block doesn't exists in your current version, some feature may not working correctly.");

            }

            Class<?> worldType = CLASS_RESOLVER.resolve("world.level.World", "World");
            MethodResolver methodResolver = new MethodResolver(worldType);
            GET_ENTITY_BY_ID_METHOD = methodResolver.resolveWrapper(
                    new ResolverQuery("getEntity", int.class),
                    new ResolverQuery("a", int.class)
            );

            FieldWrapper<Float> BLOCK_SLIPPERINESS_FIELD;
            try {
                BLOCK_SLIPPERINESS_FIELD = new FieldWrapper<>(blockType.getField("frictionFactor"));
            } catch (NoSuchFieldException ex) {
                try {
                    BLOCK_SLIPPERINESS_FIELD = new FieldWrapper<>(blockType.getField("aL"));
                } catch (NoSuchFieldException ex2) {
                    try {
                        BLOCK_SLIPPERINESS_FIELD = new FieldWrapper<>(CLASS_RESOLVER.resolve("world.level.block.state.BlockBase", "BlockBase").getDeclaredField("aL"));
                    } catch (NoSuchFieldException ex3) {
                        BLOCK_SLIPPERINESS_FIELD = new FieldWrapper<>(CLASS_RESOLVER.resolve("world.level.block.state.BlockBase", "BlockBase").getDeclaredField("frictionFactor"));
                    }
                }
            }

            NormalImplementation.BLOCK_SLIPPERINESS_FIELD = BLOCK_SLIPPERINESS_FIELD;

            CHUNK_COORD_PAIR_TYPE = CLASS_RESOLVER.resolve("world.level.ChunkCoordIntPair", "ChunkCoordIntPair");
            CHUNK_COORD_PAIR_CONSTRUCTOR = new ConstructorWrapper<>(CHUNK_COORD_PAIR_TYPE.getConstructor(int.class, int.class));

            Class<?> entityHumanType = CLASS_RESOLVER.resolve("world.entity.player.EntityHuman", "EntityHuman");
            Class<?> spawnNamedEntityType = CLASS_RESOLVER.resolve("network.protocol.game.PacketPlayOutNamedEntitySpawn", "PacketPlayOutNamedEntitySpawn");
            SPAWN_NAMED_ENTITY_CONSTRUCTOR = new ConstructorWrapper<>(spawnNamedEntityType.getConstructor(entityHumanType));

            Class<?> destoryEntityType = CLASS_RESOLVER.resolve("network.protocol.game.PacketPlayOutEntityDestroy", "PacketPlayOutEntityDestroy");
            DESTROY_ENTITY_CONSTRUCTOR = new ConstructorWrapper<>(destoryEntityType.getConstructor(int[].class));

        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

    }

    @Override
    public Entity getEntity(UUID uuid) {
        try {
            return MinecraftReflection.getBukkitEntity(MINECRAFT_SERVER.invoke("a", uuid));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Entity getEntity(World world, int id) {
        try {
            Object worldHandle = MinecraftReflection.getHandle(world);
            return MinecraftReflection.getBukkitEntity(GET_ENTITY_BY_ID_METHOD.invoke(worldHandle, id));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object toBlockNMS(MaterialData materialData) {
//        Object block = BLOCK_GET_BY_ID_METHOD.invoke(null, materialData.getItemTypeId());
//        return FROM_LEGACY_DATA_METHOD.invoke(block, materialData.getData());
        throw new UnsupportedOperationException(); // TODO - fix
    }

    @Override
    public void showDyingNPC(Player player) {
        Location location = player.getLocation();
        final List<Player> players = this.getPlayerRadius(location, 32);

        Object entityPlayer = MinecraftReflection.getHandleSilent(player);

        PacketWrapper packet = new PacketWrapper(SPAWN_NAMED_ENTITY_CONSTRUCTOR.newInstance(entityPlayer));
        int i = MinecraftReflection.getNewEntityId();
        packet.setPacketValue("a", i);

        PacketWrapper statusPacket = PacketWrapper.createByPacketName("PacketPlayOutEntityStatus");
        statusPacket.setPacketValue("a", i);
        statusPacket.setPacketValue("b", (byte) 3);
        PacketWrapper destroyPacket = new PacketWrapper(DESTROY_ENTITY_CONSTRUCTOR.newInstance(new int[]{i}));

        for (Player other : players) {
//            ((CraftPlayer) other).getHandle().playerConnection.fakeEntities.add(i); // TODO
            MinecraftReflection.sendPacket(other, packet);
            MinecraftReflection.sendPacket(other, statusPacket);
        }

        Task.runMainLater(() -> players.forEach(other -> {
//            ((CraftPlayer) other).getHandle().playerConnection.fakeEntities.remove(i); // TODO
            MinecraftReflection.sendPacket(other, destroyPacket);
        }), 20L);
    }

    @Override
    public List<Player> getPlayerRadius(Location location, double radius) {
        return location.getWorld().getNearbyEntities(location, radius / 2, radius / 2, radius / 2)
                .stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .collect(Collectors.toList());
    }

    @Override
    public void setFakeBlocks(Player player, Map<BlockPosition, MaterialData> blockMap, List<BlockPosition> replace, boolean send) {
        ConcurrentMap<BlockPosition, MaterialData> fakeBlockMap = Metadata.provideForPlayer(player).getOrNull(FAKE_BLOCK_MAP);
        if (fakeBlockMap == null) {
            fakeBlockMap = new ConcurrentHashMap<>();
            Metadata.provideForPlayer(player).put(FAKE_BLOCK_MAP, fakeBlockMap);
        }

        HashMultimap<CoordXZ, BlockPositionData> map = HashMultimap.create();
        for (final Map.Entry<BlockPosition, MaterialData> entry : blockMap.entrySet()) {
            final BlockPosition blockPosition = entry.getKey();
            MaterialData materialData = entry.getValue();
            if (materialData == null) {
                materialData = new MaterialData(Material.AIR); // TODO fix
            }
            final MaterialData previous = fakeBlockMap.put(blockPosition, materialData);
            if (send && previous != materialData) {
                final int x = blockPosition.getX();
                final int y = blockPosition.getY();
                final int z = blockPosition.getZ();
                final int chunkX = x >> 4;
                final int chunkZ = z >> 4;
                final int posX = x - (chunkX << 4);
                final int posZ = z - (chunkZ << 4);
                map.put(new CoordXZ(chunkX, chunkZ), new BlockPositionData(new BlockPosition(posX, y, posZ), materialData));
            }
        }
        for (final BlockPosition blockPosition : replace) {
            if (fakeBlockMap.remove(blockPosition) != null) {
                final int x2 = blockPosition.getX();
                final int y2 = blockPosition.getY();
                final int z2 = blockPosition.getZ();
                final org.bukkit.block.Block blockData = player.getWorld().getBlockAt(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
                final Material type = blockData.getType();
                final int data = blockData.getData();
                final int chunkX2 = x2 >> 4;
                final int chunkZ2 = z2 >> 4;
                final int posX2 = x2 - (chunkX2 << 4);
                final int posZ2 = z2 - (chunkZ2 << 4);
                map.put(new CoordXZ(chunkX2, chunkZ2), new BlockPositionData(new BlockPosition(posX2, y2, posZ2), new MaterialData(type, (byte) data))); // TODO - fix
            }
        }
        if (send) {
            for (final Map.Entry<CoordXZ, Collection<BlockPositionData>> entry2 : map.asMap().entrySet()) {
                final CoordXZ chunkPosition = entry2.getKey();
                final Collection<BlockPositionData> blocks = entry2.getValue();

                PacketWrapper packet = PacketWrapper.createByPacketName("PacketPlayOutMultiBlockChange");
                Object info = Array.newInstance(BLOCK_INFO_TYPE, blocks.size());

                int i = 0;
                for (BlockPositionData positionData : blocks) {
                    BlockPosition b = positionData.getBlockPosition();
                    MaterialData materialData = positionData.getMaterialData();

                    short s = (short) ((b.getX() & 15) << 12 | (b.getZ() & 15) << 8 | b.getY());
                    Object blockNMS = this.toBlockNMS(materialData);

                    Array.set(info, i, BLOCK_INFO_CONSTRUCTOR.resolve(
                            new Object[]{s, blockNMS},
                            new Object[]{packet.getPacket(), s, blockNMS}
                    ));
                    i++;
                }

                packet.setPacketValueByType(CHUNK_COORD_PAIR_TYPE, CHUNK_COORD_PAIR_CONSTRUCTOR.newInstance(chunkPosition.x, chunkPosition.z));

                packet.setPacketValueByType(info.getClass(), info);

                MinecraftReflection.sendPacket(player, packet);
            }
        }
    }

    @Override
    public void clearFakeBlocks(Player player, boolean send) {
        ConcurrentMap<BlockPosition, MaterialData> fakeBlockMap = Metadata.provideForPlayer(player).getOrNull(FAKE_BLOCK_MAP);
        if (fakeBlockMap == null) {
            return;
        }

        if (send) {
            this.setFakeBlocks(player, Collections.emptyMap(), new ArrayList<>(fakeBlockMap.keySet()), true);
        } else {
            fakeBlockMap.clear();
        }
    }

    private Class<?> CHAT_BASE_COMPONENT_TYPE;
    private ConstructorWrapper<?> PACKET_CHAT_CONSTRUCTOR;
    private MethodWrapper<?> CHAT_SERIALIZER_A;

    @Override
    public void sendActionBar(Player player, String message) {
        if (CHAT_BASE_COMPONENT_TYPE == null) {
            NMSClassResolver CLASS_RESOLVER = new NMSClassResolver();

            try {
                CHAT_BASE_COMPONENT_TYPE = CLASS_RESOLVER.resolve("network.chat.IChatBaseComponent", "IChatBaseComponent");

                Class<?> CHAT_SERIALIZER_TYPE = CLASS_RESOLVER.resolve("network.chat.IChatBaseComponent$ChatSerializer", "IChatBaseComponent$ChatSerializer");

                CHAT_SERIALIZER_A = new MethodWrapper<>(CHAT_SERIALIZER_TYPE.getMethod("a", String.class));
                Class<?> PACKET_PLAY_OUT_CHAT_TYPE = CLASS_RESOLVER.resolve("network.protocol.game.PacketPlayOutChat", "PacketPlayOutChat");

                PACKET_CHAT_CONSTRUCTOR = new ConstructorWrapper<>(PACKET_PLAY_OUT_CHAT_TYPE.getConstructor(CHAT_BASE_COMPONENT_TYPE, byte.class));

            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }

        Object chatComponent = CHAT_SERIALIZER_A.invoke(null, "{\"text\": \"" +
                ChatColor.translateAlternateColorCodes('&', message) + "\"}");

        Object packet = PACKET_CHAT_CONSTRUCTOR.newInstance(chatComponent, (byte) 2);

        MinecraftReflection.sendPacket(player, packet);
    }

    @Override
    public float getBlockSlipperiness(Material material) {
        Object block = BLOCK_GET_BY_ID_METHOD.invoke(null);
        return BLOCK_SLIPPERINESS_FIELD.get(block);
    }

    @Override
    public void sendEntityAttach(Player player, int type, int toAttach, int attachTo) {

        PacketWrapper packetWrapper = PacketWrapper.createByPacketName("PacketPlayOutAttachEntity");
        packetWrapper.setPacketValue("a", type);
        packetWrapper.setPacketValue("b", toAttach);
        packetWrapper.setPacketValue("c", attachTo);

        MinecraftReflection.sendPacket(player, packetWrapper);

    }

    @Override
    public void sendEntityTeleport(Player player, Location location, int id) {

        PacketWrapper packet = PacketWrapper.createByPacketName("PacketPlayOutEntityTeleport");

        packet.setPacketValue("a", id);
        packet.setPacketValue("b", (int) (location.getX() * 32.0D));
        packet.setPacketValue("c", (int) (location.getY() * 32.0D));
        packet.setPacketValue("d", (int) (location.getZ() * 32.0D));

        packet.setPacketValue("e", (byte) ((int) (location.getYaw() * 256.0F / 360.0F)));
        packet.setPacketValue("f", (byte) ((int) (location.getPitch() * 256.0F / 360.0F)));

        MinecraftReflection.sendPacket(player, packet);

    }

    @Override
    public void setSkullGameProfile(ItemMeta itemMeta, Player player) {
        if (!(itemMeta instanceof SkullMeta)) {
            throw new ClassCastException(itemMeta.getClass().getSimpleName() + " cannot be cast to " + SkullMeta.class.getSimpleName());
        }

        final Object handle = MinecraftReflection.getHandleSilent(player);
        final Object profile = GET_PROFILE_ENTITY_HUMAN_METHOD.invoke(handle);

        GAME_PROFILE_FIELD.set(itemMeta, profile);
    }

    @Override
    public boolean isServerThread() {
        return Bukkit.isPrimaryThread();
    }

    @Override
    public boolean callMoveEvent(Player player, Location from, Location to) {
        PlayerMoveEvent event = new PlayerMoveEvent(player, from, to);
        Imanity.callEvent(event);
        return !event.isCancelled();
    }

    @Override
    public AbstractMovementImplementation movement(MovementListener movementListener) {
        return new BukkitMovementImplementation(movementListener);
    }
}
