package io.fairyproject.mc.protocol;

interface PacketProviderFactory {
    PacketProviderFactory setPacketListener(InternalPacketListener packetListener);

    PacketProviderFactory setLowLevelPacketListener(InternalBufferListener bufferListener);

    void verify();

    PacketProvider build();
}
