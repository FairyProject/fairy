package io.fairyproject.bukkit.mc.entity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.fairyproject.bukkit.reflection.MinecraftReflection;
import io.fairyproject.bukkit.reflection.resolver.ConstructorResolver;
import io.fairyproject.bukkit.reflection.resolver.FieldResolver;
import io.fairyproject.bukkit.reflection.resolver.MethodResolver;
import io.fairyproject.bukkit.reflection.resolver.minecraft.NMSClassResolver;
import io.fairyproject.bukkit.reflection.wrapper.ConstructorWrapper;
import io.fairyproject.bukkit.reflection.wrapper.FieldWrapper;
import io.fairyproject.bukkit.reflection.wrapper.MethodWrapper;
import io.netty.buffer.ByteBuf;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;

@UtilityClass
public class BukkitDataWatcherConverter {

    private FieldWrapper<?> DATA_WATCHER_FIELD;
    private MethodWrapper<?> DATA_WATCHER_WRITE_METHOD;
    private ConstructorWrapper<?> PACKET_DATA_SERIALIZER_CONSTRUCTOR;

    public static @NotNull List<EntityData> convert(@NotNull Entity entity) throws ReflectiveOperationException {
        if (DATA_WATCHER_FIELD == null) {
            Class<?> entityClass = getEntityClass();
            Class<?> dataWatcherClass = getDataWatcherClass();
            Class<?> packetDataSerializerClass = getPacketDataSerializerClass();

            Field field = new FieldResolver(entityClass).resolveByFirstType(dataWatcherClass);
            field.setAccessible(true);
            DATA_WATCHER_FIELD = new FieldWrapper<>(field);

            DATA_WATCHER_WRITE_METHOD = new MethodResolver(dataWatcherClass).resolve(0, packetDataSerializerClass);
            PACKET_DATA_SERIALIZER_CONSTRUCTOR = new ConstructorResolver(packetDataSerializerClass).resolveWrapper(new Class[] {ByteBuf.class});
        }

        Object handle = MinecraftReflection.getHandle(entity);
        Object dataWatcher = DATA_WATCHER_FIELD.get(handle);

        Object byteBuf = PacketEvents.getAPI().getNettyManager().getByteBufAllocationOperator().buffer();
        Object packetDataSerializer = PACKET_DATA_SERIALIZER_CONSTRUCTOR.newInstance(byteBuf);

        DATA_WATCHER_WRITE_METHOD.invoke(dataWatcher, packetDataSerializer);
        PacketWrapper<?> packetWrapper = PacketWrapper.createUniversalPacketWrapper(byteBuf);

        return packetWrapper.readEntityMetadata();
    }

    public static Class<?> getPacketDataSerializerClass() throws ClassNotFoundException {
        return new NMSClassResolver().resolve("network.PacketDataSerializer", "network.FriendlyByteBuf", "PacketDataSerializer");
    }

    public static Class<?> getEntityClass() throws ClassNotFoundException {
        return new NMSClassResolver().resolve("world.entity.Entity", "Entity");
    }

    public static Class<?> getDataWatcherClass() throws ClassNotFoundException {
        return new NMSClassResolver().resolve(
                "network.syncher.DataWatcher",
                "network.syncher.SynchedEntityData",
                "DataWatcher"
        );
    }

}
