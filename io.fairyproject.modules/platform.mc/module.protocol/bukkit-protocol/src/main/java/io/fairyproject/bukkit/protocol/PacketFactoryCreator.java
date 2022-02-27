package io.fairyproject.bukkit.protocol;

public interface PacketFactoryCreator<W> {
    default W createEmpty() {
            throw new IllegalStateException("Not Implemented.");
        }
}