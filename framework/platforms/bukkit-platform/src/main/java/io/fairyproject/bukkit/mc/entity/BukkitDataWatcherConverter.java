package io.fairyproject.bukkit.mc.entity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.util.reflection.Reflection;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.fairyproject.bukkit.nms.BukkitNMSManager;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class BukkitDataWatcherConverter {

    private final BukkitNMSManager bukkitNMSManager;

    public Class<?> getPacketDataSerializerClass() throws ClassNotFoundException {
        return this.bukkitNMSManager.getNmsClassResolver().resolve("network.PacketDataSerializer", "network.FriendlyByteBuf", "PacketDataSerializer");
    }

    public Class<?> getEntityClass() throws ClassNotFoundException {
        return this.bukkitNMSManager.getNmsClassResolver().resolve("world.entity.Entity", "Entity");
    }

    public Class<?> getDataWatcherClass() throws ClassNotFoundException {
        return this.bukkitNMSManager.getNmsClassResolver().resolve(
                "network.syncher.DataWatcher",
                "network.syncher.SynchedEntityData",
                "DataWatcher"
        );
    }

    public @NotNull List<EntityData<?>> convert(@NotNull Entity entity) throws ReflectiveOperationException {
        if (SpigotReflectionUtil.LEGACY_DATA_WATCHER_WRITE_METHOD != null) {
            return SpigotConversionUtil.getEntityMetadata(entity);
        }

        // PacketEvents 2.14 assumes a dense array. Older Paper has null slots,
        // while Spigot can store entries in a map. Read every entry without packDirty().
        Object handle = SpigotReflectionUtil.getNMSEntity(entity);
        Object watcher = SpigotReflectionUtil.DATA_WATCHER_FIELD.get(handle);
        Method legacyWrite = Reflection.getMethod(watcher.getClass(), void.class, 0,
                List.class, SpigotReflectionUtil.NMS_PACKET_DATA_SERIALIZER_CLASS);
        List<Object> values = collectDataValues(watcher, SpigotReflectionUtil.DATA_WATCHER_ITEM_CLASS,
                legacyWrite == null ? SpigotReflectionUtil.GET_DATA_VALUE_FROM_DATA_ITEM_METHOD : null);
        Object buffer = PacketEvents.getAPI().getNettyManager().getByteBufAllocationOperator().buffer();
        try {
            Object serializer = SpigotReflectionUtil.createPacketDataSerializer(buffer);
            Method write = legacyWrite != null ? legacyWrite
                    : SpigotReflectionUtil.CLIENTBOUND_SET_ENTITY_DATA_PACKET_WRITE_DATA_WATCHER_METHOD;
            if (write == null) {
                // Before 1.20.5, pack accepts FriendlyByteBuf instead of RegistryFriendlyByteBuf.
                Class<?> packetClass = this.bukkitNMSManager.getNmsClassResolver().resolve(
                        "network.protocol.game.PacketPlayOutEntityMetadata",
                        "network.protocol.game.ClientboundSetEntityDataPacket", "PacketPlayOutEntityMetadata");
                write = Reflection.getMethod(packetClass,
                        void.class, 0, List.class, SpigotReflectionUtil.NMS_PACKET_DATA_SERIALIZER_CLASS);
            }
            if (write == null) {
                throw new NoSuchMethodException("Entity metadata packet writer");
            }
            write.invoke(null, values, serializer);
            return PacketWrapper.createUniversalPacketWrapper(buffer).readEntityMetadata();
        } finally {
            ByteBufHelper.release(buffer);
        }
    }

    static List<Object> collectDataValues(Object watcher, Class<?> itemClass, Method getValue)
            throws ReflectiveOperationException {
        for (Field field : watcher.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            Class<?> type = field.getType();
            boolean array = type.isArray() && type.getComponentType() == itemClass;
            if (!array && !Map.class.isAssignableFrom(type)) {
                continue;
            }
            field.setAccessible(true);
            Object storage = field.get(watcher);
            Collection<?> items = array ? Arrays.asList((Object[]) storage) : ((Map<?, ?>) storage).values();
            List<Object> values = new ArrayList<>(items.size());
            for (Object item : items) {
                if (item != null) {
                    values.add(getValue == null ? item : getValue.invoke(item));
                }
            }
            return values;
        }
        throw new NoSuchFieldException("Entity metadata storage in " + watcher.getClass().getName());
    }

}
