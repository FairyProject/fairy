package io.fairyproject.bukkit.mc;

import io.fairyproject.bukkit.protocol.BukkitNettyInjector;
import io.fairyproject.bukkit.util.Players;
import io.fairyproject.mc.*;
import io.fairyproject.mc.protocol.mapping.MCProtocolMapping;
import io.fairyproject.mc.protocol.mapping.MCProtocolMapping1_8;
import io.fairyproject.mc.protocol.netty.NettyInjector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class BukkitMCInitializer implements MCInitializer {

    @Override
    public NettyInjector createNettyInjector() {
        return new BukkitNettyInjector();
    }

    @Override
    public MCProtocolMapping createProtocolMapping() {
        return new MCProtocolMapping1_8(); // TODO
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
        };
    }

    @Override
    public MCPlayer.Bridge createPlayerBridge() {
        return new MCPlayer.Bridge() {
            @Override
            public UUID from(Object obj) {
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
                    throw new IllegalArgumentException();
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
