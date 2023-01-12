package io.fairyproject.bukkit.mc;

import io.fairyproject.bukkit.mc.entity.BukkitDataWatcherConverter;
import io.fairyproject.bukkit.mc.entity.BukkitEntityIDCounter;
import io.fairyproject.bukkit.mc.operator.BukkitMCPlayerOperator;
import io.fairyproject.bukkit.mc.operator.BukkitMCPlayerOperatorImpl;
import io.fairyproject.bukkit.metadata.Metadata;
import io.fairyproject.bukkit.nms.BukkitNMSManager;
import io.fairyproject.bukkit.nms.BukkitNMSManagerImpl;
import io.fairyproject.bukkit.reflection.MinecraftReflection;
import io.fairyproject.bukkit.util.Players;
import io.fairyproject.container.Containers;
import io.fairyproject.mc.*;
import io.fairyproject.mc.entity.EntityIDCounter;
import io.fairyproject.mc.version.MCVersionMappingRegistry;
import io.fairyproject.metadata.MetadataKey;
import lombok.Getter;
import net.kyori.adventure.text.serializer.gson.legacyimpl.NBTLegacyHoverEventSerializer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class BukkitMCInitializer implements MCInitializer {

    private BukkitNMSManager nmsManager;
    private BukkitMCPlayerOperator playerOperator;
    private BukkitDataWatcherConverter dataWatcherConverter;

    @Override
    public void serverLoaded() {
        this.nmsManager = this.createNMSManager();
        this.playerOperator = new BukkitMCPlayerOperatorImpl(nmsManager);
        MinecraftReflection.init(nmsManager);
        this.dataWatcherConverter = new BukkitDataWatcherConverter(nmsManager);

        EntityIDCounter.Companion.CURRENT = new BukkitEntityIDCounter(nmsManager);
    }

    public BukkitNMSManager createNMSManager() {
        return new BukkitNMSManagerImpl(MCServer.current(), Containers.get(MCVersionMappingRegistry.class), Bukkit.getServer().getClass());
    }

    @Override
    public MCAdventure.AdventureHook createAdventure() {
        return MCAdventure.AdventureHook.builder()
                .serializer(NBTLegacyHoverEventSerializer.get())
                .build();
    }

    @Override
    public MCServer createMCServer() {
        return new BukkitMCServer(Bukkit.getServer(), this.nmsManager);
    }

    @Override
    public MCEntity.Bridge createEntityBridge() {

        return new MCEntity.Bridge() {
            @Override
            public MCEntity from(Object entity) {
                if (!(entity instanceof org.bukkit.entity.Entity)) {
                    throw new UnsupportedOperationException();
                }
                return new BukkitMCEntity((org.bukkit.entity.Entity) entity, dataWatcherConverter);
            }
        };
    }

    @Override
    public MCWorld.Bridge createWorldBridge() {
        return new MCWorld.Bridge() {

            private final MetadataKey<MCWorld> KEY = MetadataKey.create("fairy:mc-world", MCWorld.class);

            @Override
            public MCWorld from(Object worldObj) {
                if (!(worldObj instanceof World)) {
                    throw new UnsupportedOperationException();
                }
                World world = (World) worldObj;
                return Metadata.provideForWorld(world).getOrPut(KEY, () -> new BukkitMCWorld(world));
            }

            @Override
            public MCWorld getByName(String name) {
                final World world = Bukkit.getWorld(name);
                if (world == null) {
                    return null;
                }
                return this.from(world);
            }

            @Override
            public List<MCWorld> all() {
                return Bukkit.getWorlds().stream()
                        .map(this::from)
                        .collect(Collectors.toList());
            }
        };
    }

    @Override
    public MCPlayer.Bridge createPlayerBridge(MCVersionMappingRegistry versionMappingRegistry) {
        MCServer mcServer = MCServer.current();

        return new MCPlayer.Bridge() {
            @Override
            public UUID from(@NotNull Object obj) {
                return Players.tryGetUniqueId(obj);
            }

            @Override
            public MCPlayer find(UUID uuid) {
                final Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    return MCPlayer.from(player);
                }
                return null;
            }

            @Override
            public MCPlayer create(Object obj) {
                if (!(obj instanceof Player)) {
                    if (obj instanceof UUID) {
                        final Player player = Bukkit.getPlayer((UUID) obj);
                        if (player != null)
                            return create(player);
                    }
                    throw new IllegalArgumentException(obj.getClass().getName());
                }
                return new BukkitMCPlayer((Player) obj, mcServer, dataWatcherConverter, playerOperator, versionMappingRegistry);
            }

            @Override
            public Collection<MCPlayer> all() {
                return Bukkit.getOnlinePlayers().stream()
                        .map(MCPlayer::from)
                        .collect(Collectors.toList());
            }
        };
    }

    @Override
    public MCGameProfile.Bridge createGameProfileBridge() {
        return new MCGameProfile.Bridge() {
            @Override
            public MCGameProfile create(String name, UUID uuid) {
                return new BukkitMCGameProfile(name, uuid);
            }

            @Override
            public MCGameProfile from(Object object) {
                return BukkitMCGameProfile.CONVERTER.getSpecific(object);
            }
        };
    }
}
