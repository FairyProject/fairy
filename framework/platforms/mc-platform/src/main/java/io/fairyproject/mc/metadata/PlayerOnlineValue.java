package io.fairyproject.mc.metadata;

import io.fairyproject.mc.MCPlayer;
import io.fairyproject.metadata.TransientValue;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class PlayerOnlineValue<T> implements TransientValue<T> {

    private final T value;
    private final MCPlayer ownedPlayer;

    public static <T> TransientValue<T> create(T value, UUID ownedPlayerUuid) {
        return PlayerOnlineValue.create(value, Objects.requireNonNull(MCPlayer.find(ownedPlayerUuid)));
    }

    public static <T> TransientValue<T> create(T value, MCPlayer ownedPlayer) {
        return new PlayerOnlineValue<>(value, ownedPlayer);
    }

    private PlayerOnlineValue(T value, MCPlayer ownedPlayer) {
        this.value = value;
        this.ownedPlayer = ownedPlayer;
    }

    @Nullable
    @Override
    public T getOrNull() {
        return shouldExpire() ? null : this.value;
    }

    @Override
    public boolean shouldExpire() {
        return ownedPlayer == null || !ownedPlayer.isOnline();
    }
}
