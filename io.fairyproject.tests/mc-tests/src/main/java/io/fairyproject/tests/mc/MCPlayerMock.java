package io.fairyproject.tests.mc;

import io.fairyproject.mc.GameMode;
import io.fairyproject.mc.MCGameProfile;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.MCVersion;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@RequiredArgsConstructor
public class MCPlayerMock implements MCPlayer {

    private final UUID uuid;
    private final String name;
    private final MCVersion version;
    private final Object originalInstance;

    @Nullable
    private Component displayName;
    @Setter
    private String locale = "en_us";
    @Setter
    private boolean online = true;
    @Setter
    private int ping = 0;
    @Setter
    private GameMode gameMode = GameMode.SURVIVAL;

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isOnline() {
        return this.online;
    }

    @Override
    public Component getDisplayName() {
        return this.displayName == null ? Component.text(this.name) : this.displayName;
    }

    @Override
    public void setDisplayName(Component component) {
        this.displayName = component;
    }

    @Override
    public MCVersion getVersion() {
        return version;
    }

    @Override
    public int ping() {
        return this.ping;
    }

    @Override
    public GameMode gameMode() {
        return this.gameMode;
    }

    @Override
    public MCGameProfile gameProfile() {
        return MCGameProfile.create(this.uuid, this.name);
    }

    @Override
    public String getGameLocale() {
        return this.locale;
    }

    @Override
    public Channel getChannel() {
        throw new UnsupportedOperationException("Not Implemented.");
    }

    @Override
    public int getProtocolId() {
        return this.version.getRawVersion()[0];
    }

    @Override
    public <T> T as(Class<T> playerClass) {
        if (playerClass.isAssignableFrom(originalInstance.getClass())) {
            return playerClass.cast(this.originalInstance);
        }
        throw new IllegalArgumentException();
    }
}
