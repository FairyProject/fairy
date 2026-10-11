package io.fairyproject.bukkit.mc.entity;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import io.fairyproject.bukkit.nms.BukkitNMSManager;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
        // PacketEvents handles both legacy DataWatcher and modern SynchedEntityData
        // without consuming the entity's dirty metadata or leaking its byte buffer.
        return SpigotConversionUtil.getEntityMetadata(entity);
    }

}
