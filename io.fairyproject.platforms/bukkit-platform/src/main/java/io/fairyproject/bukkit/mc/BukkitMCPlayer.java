package io.fairyproject.bukkit.mc;

import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.bukkit.mc.entity.BukkitDataWatcherConverter;
import io.fairyproject.bukkit.mc.operator.BukkitMCPlayerOperator;
import io.fairyproject.bukkit.util.PlayerLocaleUtil;
import io.fairyproject.mc.GameMode;
import io.fairyproject.mc.MCGameProfile;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.MCServer;
import io.fairyproject.mc.util.AudienceProxy;
import io.fairyproject.mc.version.MCVersion;
import io.fairyproject.mc.version.MCVersionMapping;
import io.fairyproject.mc.version.MCVersionMappingRegistry;
import io.netty.channel.Channel;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BukkitMCPlayer extends BukkitMCEntity implements AudienceProxy, MCPlayer {

    private final Player player;
    private final Channel channel;
    private final Audience audience;
    private final MCServer server;
    private final BukkitMCPlayerOperator operator;
    private final MCVersionMappingRegistry versionMappingRegistry;

    public BukkitMCPlayer(
            Player player,
            MCServer server,
            BukkitDataWatcherConverter dataWatcherConverter,
            BukkitMCPlayerOperator operator,
            MCVersionMappingRegistry versionMappingRegistry
    ) {
        super(player, dataWatcherConverter);
        this.player = player;
        this.server = server;
        this.operator = operator;
        this.versionMappingRegistry = versionMappingRegistry;
        this.audience = FairyBukkitPlatform.AUDIENCES.player(player);
        this.channel = operator.getChannel(player);
    }

    @Override
    public MCVersion getVersion() {
        MCVersion version = this.server.getVersion();
        MCVersionMapping mapping = this.versionMappingRegistry.findMapping(version);

        return mapping.toMCVersion();
    }

    @Override
    public int getPing() {
        return this.operator.getPing(this.player);
    }

    @Override
    public GameMode getGameMode() {
        return GameMode.valueOf(this.player.getGameMode().name());
    }

    @Override
    public MCGameProfile getGameProfile() {
        return this.operator.getGameProfile(player);
    }

    @Override
    public UUID getUUID() {
        return this.player.getUniqueId();
    }

    @Override
    public int getId() {
        return this.player.getEntityId();
    }

    @Override
    public String getName() {
        return this.player.getName();
    }

    @Override
    public boolean isOnline() {
        return this.player.isOnline();
    }

    @Override
    public Component getDisplayName() {
        return this.operator.getDisplayName(player);
    }

    @Override
    public void setDisplayName(Component component) {
        this.operator.setDisplayName(this, component);
    }

    @Override
    public String getGameLocale() {
        return PlayerLocaleUtil.getLocale(this.player);
    }

    @Override
    public Channel getChannel() {
        return this.channel;
    }

    @Override
    public <T> T as(Class<T> playerClass) {
        if (!playerClass.isInstance(this.player)) {
            throw new ClassCastException();
        }
        return playerClass.cast(this.player);
    }


    @Override
    public Audience audience() {
        return this.audience;
    }
}
