package io.fairyproject.bukkit.mc;

import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.bukkit.mc.entity.BukkitDataWatcherConverter;
import io.fairyproject.bukkit.mc.operator.BukkitMCPlayerOperator;
import io.fairyproject.bukkit.util.PlayerLocaleUtil;
import io.fairyproject.mc.GameMode;
import io.fairyproject.mc.MCGameProfile;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.MCServer;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.util.AudienceProxy;
import io.fairyproject.mc.version.MCVersion;
import io.fairyproject.mc.version.MCVersionMapping;
import io.fairyproject.mc.version.MCVersionMappingRegistry;
import io.netty.channel.Channel;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.util.UUID;

public class BukkitMCPlayer extends BukkitMCEntity implements AudienceProxy, MCPlayer {

    private Player player;
    private Channel channel;
    private Audience audience;

    private final UUID uuid;
    private final String name;
    private final InetAddress address;
    private final MCServer server;
    private final BukkitMCPlayerOperator operator;
    private final MCVersionMappingRegistry versionMappingRegistry;

    public BukkitMCPlayer(
            UUID uuid,
            String name,
            InetAddress address,
            MCServer server,
            MCProtocol protocol,
            BukkitDataWatcherConverter dataWatcherConverter,
            BukkitMCPlayerOperator operator,
            MCVersionMappingRegistry versionMappingRegistry
    ) {
        super(dataWatcherConverter);
        this.uuid = uuid;
        this.name = name;
        this.address = address;
        this.server = server;
        this.operator = operator;
        this.versionMappingRegistry = versionMappingRegistry;
    }

    @Override
    public InetAddress getAddress() {
        return this.address;
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
        return this.uuid;
    }

    @Override
    public int getId() {
        return this.player.getEntityId();
    }

    @Override
    public String getName() {
        return this.name;
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
        if (this.channel == null) {
            this.channel = this.operator.getChannel(this.player);
        }
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
    public void setNative(@NotNull Object nativeObject) {
        super.setNative(nativeObject);
        this.player = (Player) nativeObject;
        this.audience = FairyBukkitPlatform.AUDIENCES.player(player);
    }

    @Override
    public Audience audience() {
        return this.audience;
    }
}
