package io.fairyproject.mc.protocol.mapping;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.fairyproject.mc.protocol.MCPacket;
import io.fairyproject.mc.protocol.netty.FriendlyByteBuf;
import io.fairyproject.mc.protocol.packet.PacketDeserializer;
import io.fairyproject.mc.protocol.packet.PacketDirection;
import io.fairyproject.mc.protocol.MCVersion;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public abstract class MCProtocolMapping {

    protected final Map<Integer, Protocol> protocols = new HashMap<>();

    public Protocol getProtocol(int id) {
        return this.protocols.get(id);
    }

    public void registerProtocol(int id, Protocol protocol) {
        this.protocols.put(id, protocol);
    }

    public abstract MCVersion getVersion();

    public interface Protocol {

        Class<? extends MCPacket> fromId(int id, PacketDirection direction);

        int fromPacketClass(Class<? extends MCPacket> packetClass);

    }

    public static abstract class AbstractProtocol implements Protocol {

        private final EnumMap<PacketDirection, Int2ObjectMap<PacketDeserializer>> deserializers;
        private final EnumMap<PacketDirection, BiMap<Class<? extends MCPacket>, Integer>> packets;

        public AbstractProtocol() {
            this.deserializers = new EnumMap<>(PacketDirection.class);
            this.packets = new EnumMap<>(PacketDirection.class);
            this.init();
        }

        private Int2ObjectMap<PacketDeserializer> getDeserializers(PacketDirection direction) {
            return this.deserializers.computeIfAbsent(direction, ignored -> new Int2ObjectOpenHashMap<>());
        }

        private BiMap<Class<? extends MCPacket>, Integer> getPackets(PacketDirection direction) {
            return this.packets.computeIfAbsent(direction, ignored -> HashBiMap.create());
        }

        public abstract void init();

        public MCPacket deserialize(PacketDirection direction, FriendlyByteBuf byteBuf, int id) {
            final PacketDeserializer packetDeserializer = this.getDeserializers(direction).getOrDefault(id, PacketDeserializer.DEFAULT);
            final Class<? extends MCPacket> packetClass = this.fromId(id, direction);

            return packetDeserializer.deserialize(this, byteBuf, id, packetClass);
        }

        public void registerInDeserializer(int id, PacketDeserializer deserializer) {
            this.getDeserializers(PacketDirection.IN).put(id, deserializer);
        }

        public void registerOutDeserializer(int id, PacketDeserializer deserializer) {
            this.getDeserializers(PacketDirection.OUT).put(id, deserializer);
        }

        public void registerIn(int id, Class<? extends MCPacket> packetClass) {
            this.getPackets(PacketDirection.IN).put(packetClass, id);
        }

        public void registerOut(int id, Class<? extends MCPacket> packetClass) {
            this.getPackets(PacketDirection.OUT).put(packetClass, id);
        }

        @Override
        public Class<? extends MCPacket> fromId(int id, PacketDirection direction) {
            final Class<? extends MCPacket> in = this.getPackets(PacketDirection.IN).inverse().getOrDefault(id, null);
            if (in != null)
                return in;
            final Class<? extends MCPacket> out = this.getPackets(PacketDirection.OUT).inverse().getOrDefault(id, null);
            if (out != null)
                return out;
            throw new IllegalArgumentException(id + " " + direction.name());
        }

        @Override
        public int fromPacketClass(Class<? extends MCPacket> packetClass) {
            final Integer in = this.getPackets(PacketDirection.IN).getOrDefault(packetClass, null);
            if (in != null)
                return in;
            final Integer out = this.getPackets(PacketDirection.OUT).getOrDefault(packetClass, null);
            if (out != null)
                return out;
            throw new IllegalArgumentException(packetClass.getName());
        }

    }

}
