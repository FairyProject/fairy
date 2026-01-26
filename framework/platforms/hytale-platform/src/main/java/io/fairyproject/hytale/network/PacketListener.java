package io.fairyproject.hytale.network;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.jetbrains.annotations.NotNull;

public interface PacketListener<T> {

    void handle(@NotNull PlayerRef playerRef, @NotNull T packet);

}
