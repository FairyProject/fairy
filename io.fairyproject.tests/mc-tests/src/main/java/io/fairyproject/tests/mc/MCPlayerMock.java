package io.fairyproject.tests.mc;

import io.fairyproject.event.EventNode;
import io.fairyproject.event.GlobalEventNode;
import io.fairyproject.mc.GameMode;
import io.fairyproject.mc.MCEventFilter;
import io.fairyproject.mc.MCGameProfile;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.event.trait.MCEntityEvent;
import io.fairyproject.mc.version.MCVersion;
import io.fairyproject.mc.version.MCVersionMappingRegistry;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@RequiredArgsConstructor
public abstract class MCPlayerMock implements MCPlayer {

    private final UUID uuid;
    private final String name;
    private final MCVersion version;
    private final MCVersionMappingRegistry versionMappingRegistry;

    @Nullable
    private Object originalInstance;
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
    private final EventNode<MCEntityEvent> eventNode = GlobalEventNode.get().map(this, MCEventFilter.ENTITY);

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public int getId() {
        return 0;
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
    public @NotNull EventNode<MCEntityEvent> getEventNode() {
        return this.eventNode;
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
    public int getPing() {
        return this.ping;
    }

    @Override
    public GameMode getGameMode() {
        return this.gameMode;
    }

    @Override
    public MCGameProfile getGameProfile() {
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
    public <T> T as(Class<T> playerClass) {
        if (playerClass.isAssignableFrom(originalInstance.getClass())) {
            return playerClass.cast(this.originalInstance);
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void setNative(@NotNull Object nativeObject) {
        this.originalInstance = nativeObject;
    }
}
