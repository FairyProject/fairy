package io.fairyproject.bukkit.protocol;

import io.fairyproject.mc.MCPlayer;

public interface PacketFactoryWrapper<T, W> {
    T wrap(final W typeObj, final MCPlayer player);
}