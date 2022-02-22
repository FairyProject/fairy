package io.fairyproject.mc.metadata;

import io.fairyproject.mc.MCPlayer;
import io.fairyproject.metadata.TransientValue;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerOnlineValue<T> implements TransientValue<T> {

    private final T value;
    private final UUID ownedPlayerUuid;

    public static <T> TransientValue<T> create(T value, UUID ownedPlayerUuid) {
        return new PlayerOnlineValue<>(value, ownedPlayerUuid);
    }

    public static <T> TransientValue<T> create(T value, MCPlayer ownedPlayer) {
        return new PlayerOnlineValue<>(value, ownedPlayer.getUUID());
    }

    private PlayerOnlineValue(T value, UUID ownedPlayerUuid) {
        this.value = value;
        this.ownedPlayerUuid = ownedPlayerUuid;
    }

    @Nullable
    @Override
    public T getOrNull() {
        return shouldExpire() ? null : this.value;
    }

    @Override
    public boolean shouldExpire() {
        final MCPlayer mcPlayer = MCPlayer.find(this.ownedPlayerUuid);
        return mcPlayer == null || !mcPlayer.isOnline();
    }
}
