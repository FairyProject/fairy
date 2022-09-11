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

import io.fairyproject.bukkit.impl.annotation.ServerImpl;
import io.fairyproject.bukkit.listener.events.Events;
import io.fairyproject.bukkit.player.movement.MovementListener;
import io.fairyproject.bukkit.player.movement.impl.AbstractMovementImplementation;
import io.fairyproject.bukkit.player.movement.impl.BukkitMovementImplementation;
import io.fairyproject.bukkit.reflection.MinecraftReflection;
import io.fairyproject.bukkit.reflection.resolver.MethodResolver;
import io.fairyproject.bukkit.reflection.resolver.ResolverQuery;
import io.fairyproject.bukkit.reflection.resolver.minecraft.NMSClassResolver;
import io.fairyproject.bukkit.reflection.resolver.minecraft.OBCClassResolver;
import io.fairyproject.bukkit.reflection.wrapper.*;
import io.fairyproject.mc.util.BlockPosition;
import io.fairyproject.task.Task;
import io.fairyproject.util.AccessUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * TODO
 * - Modern version compatibility
 */
@ServerImpl
public class NormalImplementation implements ServerImplementation {

//    public static final MetadataKey<ConcurrentMap> FAKE_BLOCK_MAP = MetadataKey.create(Fairy.METADATA_PREFIX + "FakeBlockMap", ConcurrentMap.class);

    private static final ObjectWrapper MINECRAFT_SERVER;
    private static FieldWrapper GAME_PROFILE_FIELD;

    private static MethodWrapper<?> GET_PROFILE_ENTITY_HUMAN_METHOD;
    private static final MethodWrapper<?> GET_ENTITY_BY_ID_METHOD;

    private static final ConstructorWrapper<?> SPAWN_NAMED_ENTITY_CONSTRUCTOR;
    private static final ConstructorWrapper<?> DESTROY_ENTITY_CONSTRUCTOR;

    static {
        final NMSClassResolver CLASS_RESOLVER = new NMSClassResolver();
        final OBCClassResolver OBC_RESOLVER = new OBCClassResolver();
        try {
            final Field field = OBC_RESOLVER.resolve("inventory.CraftMetaSkull").getDeclaredField("profile");
            AccessUtil.setAccessible(field);
            GAME_PROFILE_FIELD = new FieldWrapper<>(field);
            GET_PROFILE_ENTITY_HUMAN_METHOD = new MethodResolver(CLASS_RESOLVER.resolve("world.entity.player.EntityHuman", "EntityHuman"))
                    .resolve(MinecraftReflection.GAME_PROFILE_TYPE, 0);

            Class<?> minecraftServerType = CLASS_RESOLVER.resolve("server.MinecraftServer", "MinecraftServer");
            Object minecraftServer = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
            MINECRAFT_SERVER = new ObjectWrapper(minecraftServer, minecraftServerType);

            Class<?> blockType = CLASS_RESOLVER.resolve("world.level.block.Block", "Block");
            Class<?> worldType = CLASS_RESOLVER.resolve("world.level.World", "World");
            MethodResolver methodResolver = new MethodResolver(worldType);
            GET_ENTITY_BY_ID_METHOD = methodResolver.resolveWrapper(
                    new ResolverQuery("getEntity", int.class),
                    new ResolverQuery("a", int.class)
            );

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
        throw new UnsupportedOperationException();
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
    @Deprecated
    public float getBlockSlipperiness(Material material) {
        throw new UnsupportedOperationException();
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
        Events.call(event);
        return !event.isCancelled();
    }

    @Override
    public AbstractMovementImplementation movement(MovementListener movementListener) {
        return new BukkitMovementImplementation(movementListener);
    }
}
