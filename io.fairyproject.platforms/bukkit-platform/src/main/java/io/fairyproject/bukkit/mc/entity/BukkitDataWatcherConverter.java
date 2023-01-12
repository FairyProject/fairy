package io.fairyproject.bukkit.mc.entity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.fairyproject.bukkit.nms.BukkitNMSManager;
import io.fairyproject.bukkit.reflection.MinecraftReflection;
import io.fairyproject.bukkit.reflection.resolver.ConstructorResolver;
import io.fairyproject.bukkit.reflection.resolver.FieldResolver;
import io.fairyproject.bukkit.reflection.resolver.MethodResolver;
import io.fairyproject.bukkit.reflection.resolver.ResolverQuery;
import io.fairyproject.bukkit.reflection.wrapper.ConstructorWrapper;
import io.fairyproject.bukkit.reflection.wrapper.FieldWrapper;
import io.fairyproject.bukkit.reflection.wrapper.MethodWrapper;
import io.fairyproject.util.AccessUtil;
import io.fairyproject.util.exceptionally.ThrowingFunction;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

@RequiredArgsConstructor
public class BukkitDataWatcherConverter {

    private final BukkitNMSManager bukkitNMSManager;

    private FieldWrapper<?> dataWatcherField;
    private MethodWrapper<?> dataWatcherWriteMethod;
    private ConstructorWrapper<?> packetDataSerializerConstructor;

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

    private Function<Entity, List<EntityData>> converter;

    public @NotNull List<EntityData> convert(@NotNull Entity entity) throws ReflectiveOperationException {
        if (converter == null) {
            Class<?> entityClass = getEntityClass();
            Class<?> dataWatcherClass = getDataWatcherClass();
            Class<?> packetDataSerializerClass = getPacketDataSerializerClass();
            Class<?> byteBufClass = Class.forName("io.netty.buffer.ByteBuf");
            Field field = new FieldResolver(entityClass).resolveByFirstType(dataWatcherClass);
            AccessUtil.setAccessible(field);
            FieldWrapper<?> dataWatcherField = new FieldWrapper<>(field);
            ConstructorWrapper<?> packetDataSerializerConstructor = new ConstructorResolver(packetDataSerializerClass).resolveWrapper(new Class[]{byteBufClass});

            try {
                MethodWrapper<?> dataWatcherWriteMethod = new MethodResolver(dataWatcherClass).resolve(void.class, 0, packetDataSerializerClass);

                converter = ThrowingFunction.sneaky(e -> {
                    Object handle = MinecraftReflection.getHandle(e);
                    Object dataWatcher = dataWatcherField.get(handle);

                    Object byteBuf = PacketEvents.getAPI().getNettyManager().getByteBufAllocationOperator().buffer();
                    Object packetDataSerializer = packetDataSerializerConstructor.newInstance(byteBuf);

                    dataWatcherWriteMethod.invoke(dataWatcher, packetDataSerializer);
                    PacketWrapper<?> packetWrapper = PacketWrapper.createUniversalPacketWrapper(byteBuf);

                    return packetWrapper.readEntityMetadata();
                });
            } catch (Throwable throwable) {
                // 1.13+
                Method dataWatcherPackDirtyMethod = new MethodResolver(dataWatcherClass).resolve(new ResolverQuery(List.class, 0).withModifierOptions(ResolverQuery.ModifierOptions.builder()
                        .onlyDynamic(true)
                        .build()));
                Method dataWatcherPackMethod = new MethodResolver(dataWatcherClass).resolve(new ResolverQuery(void.class, 0, List.class, packetDataSerializerClass).withModifierOptions(ResolverQuery.ModifierOptions.builder()
                        .onlyStatic(true)
                        .build()));

                converter = ThrowingFunction.sneaky(e -> {
                    Object handle = MinecraftReflection.getHandle(e);
                    Object dataWatcher = dataWatcherField.get(handle);

                    Object byteBuf = PacketEvents.getAPI().getNettyManager().getByteBufAllocationOperator().buffer();
                    Object packetDataSerializer = packetDataSerializerConstructor.newInstance(byteBuf);

                    Object dataItemList = dataWatcherPackDirtyMethod.invoke(dataWatcher);
                    dataWatcherPackMethod.invoke(null, dataItemList, packetDataSerializer);
                    PacketWrapper<?> packetWrapper = PacketWrapper.createUniversalPacketWrapper(byteBuf);

                    return packetWrapper.readEntityMetadata();
                });
            }
        }

        return converter.apply(entity);
    }

}
