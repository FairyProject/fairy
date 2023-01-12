package io.fairyproject.bukkit.mc;

import io.fairyproject.Debug;
import io.fairyproject.bukkit.nms.BukkitNMSManager;
import io.fairyproject.bukkit.reflection.MinecraftReflection;
import io.fairyproject.bukkit.version.BukkitVersionDecoder;
import io.fairyproject.mc.MCEntity;
import io.fairyproject.mc.MCServer;
import io.fairyproject.mc.version.MCVersion;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Entity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.Function;

public class BukkitMCServer implements MCServer {

    private final Server server;
    private final BukkitNMSManager nmsManager;
    private Function<UUID, Entity> uuidToEntity;

    public BukkitMCServer(Server server, BukkitNMSManager nmsManager) {
        this.server = server;
        this.nmsManager = nmsManager;
    }

    @Override
    public boolean isMainThread() {
        return Bukkit.isPrimaryThread();
    }

    @Override
    public MCEntity getEntity(UUID entityUuid) {
        if (uuidToEntity == null) {
            if (Debug.UNIT_TEST)
                throw new IllegalStateException("Unit testing unsupported.");

            Function<UUID, Entity> uuidToEntity;
            try {
                Bukkit.class.getDeclaredMethod("getEntity", UUID.class);
                uuidToEntity = Bukkit::getEntity;
            } catch (NoSuchMethodException ex) {
                try {
                    final Class<?> minecraftServer = this.nmsManager.getNmsClassResolver().resolve("server.MinecraftServer","MinecraftServer");
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
            this.uuidToEntity = uuidToEntity;
        }

        return MCEntity.from(this.uuidToEntity.apply(entityUuid));
    }

    @Override
    public MCVersion getVersion() {
        BukkitVersionDecoder bukkitVersionDecoder = BukkitVersionDecoder.create();

        return bukkitVersionDecoder.decode(this.server);
    }
}
