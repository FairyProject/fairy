package io.fairyproject.bukkit.protocol;

public interface PacketBuilder<T, W> extends PacketFactoryCreator<T>, PacketFactoryWrapper<T, W> {
}
