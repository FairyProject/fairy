package io.fairyproject.bukkit.mc;

import io.fairyproject.Debug;
import io.fairyproject.bukkit.reflection.MinecraftReflection;
import io.fairyproject.bukkit.reflection.minecraft.OBCVersion;
import io.fairyproject.bukkit.reflection.resolver.minecraft.NMSClassResolver;
import io.fairyproject.mc.MCEntity;
import io.fairyproject.mc.MCServer;
import io.fairyproject.mc.protocol.MCVersion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.Function;

public class BukkitMCServer implements MCServer {
    private static final Function<UUID, Entity> UUID_TO_ENTITY;
    static {
        Function<UUID, Entity> uuidToEntity;
        if (Debug.UNIT_TEST) {
            uuidToEntity = uuid -> {
                throw new IllegalStateException("Unit testing unsupported.");
            };
        } else {
            try {
                Bukkit.getEntity(UUID.randomUUID());
                uuidToEntity = Bukkit::getEntity;
            } catch (NoSuchMethodError ex) {
                try {
                    NMSClassResolver classResolver = new NMSClassResolver();
                    final Class<?> minecraftServer = classResolver.resolve("server.MinecraftServer","MinecraftServer");
                    final Object server = minecraftServer.getMethod("getServer").invoke(null);
                    Method method = minecraftServer.getDeclaredMethod("a", UUID.class);
                    MethodHandle methodHandle = MethodHandles.lookup().unreflect(method);

                    uuidToEntity = uuid -> {
                        try {
                            return MinecraftReflection.getBukkitEntity(methodHandle.invoke(server, uuid));
                        } catch (Throwable throwable) {
                            throw new IllegalStateException(throwable);
                        }
                    };
                } catch (Throwable throwable) {
                    throw new IllegalStateException(throwable);
                }
            }
        }

        UUID_TO_ENTITY = uuidToEntity;
    }

    @Override
    public MCEntity getEntity(UUID entityUuid) {
        return MCEntity.from(UUID_TO_ENTITY.apply(entityUuid));
    }

    @Override
    public MCVersion getVersion() {
        return OBCVersion.get().toMCVersion();
    }
}
