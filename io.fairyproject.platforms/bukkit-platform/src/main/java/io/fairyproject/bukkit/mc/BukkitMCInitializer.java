package io.fairyproject.bukkit.mc;

import io.fairyproject.bukkit.util.Players;
import io.fairyproject.mc.*;
import net.kyori.adventure.text.serializer.gson.legacyimpl.NBTLegacyHoverEventSerializer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BukkitMCInitializer implements MCInitializer {

    @Override
    public MCAdventure.AdventureHook createAdventure() {
        return MCAdventure.AdventureHook.builder()
                .serializer(NBTLegacyHoverEventSerializer.get())
                .build();
    }

    @Override
    public MCServer createMCServer() {
        return new BukkitMCServer();
    }

    @Override
    public MCEntity.Bridge createEntityBridge() {
        return new MCEntity.Bridge() {
            @Override
            public MCEntity from(Object entity) {
                if (!(entity instanceof org.bukkit.entity.Entity)) {
                    throw new UnsupportedOperationException();
                }
                return new BukkitMCEntity((org.bukkit.entity.Entity) entity);
            }

            @Override
            public int newEntityId() {
                return MinecraftReflection.getNewEntityId();
            }
        };
    }

    @Override
    public MCWorld.Bridge createWorldBridge() {
        return new MCWorld.Bridge() {
            @Override
            public MCWorld from(Object world) {
                if (!(world instanceof org.bukkit.World)) {
                    throw new UnsupportedOperationException();
                }
                return new BukkitMCWorld((org.bukkit.World) world);
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
    public MCPlayer.Bridge createPlayerBridge() {
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
                return new BukkitMCPlayer((Player) obj);
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
