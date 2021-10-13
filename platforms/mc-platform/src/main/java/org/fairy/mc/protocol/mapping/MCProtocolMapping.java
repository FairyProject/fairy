package org.fairy.mc.protocol.mapping;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.fairy.mc.protocol.MCPacket;
import org.fairy.mc.protocol.MCVersion;
import org.fairy.mc.protocol.packet.PacketDirection;

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

        private final BiMap<Integer, Class<? extends MCPacket>> inMap;
        private final BiMap<Integer, Class<? extends MCPacket>> outMap;

        public AbstractProtocol() {
            this.inMap = HashBiMap.create();
            this.outMap = HashBiMap.create();
            this.init();
        }

        public abstract void init();

        public void registerIn(int id, Class<? extends MCPacket> packetClass) {
            this.inMap.put(id, packetClass);
        }

        public void registerOut(int id, Class<? extends MCPacket> packetClass) {
            this.outMap.put(id, packetClass);
        }

        @Override
        public Class<? extends MCPacket> fromId(int id, PacketDirection direction) {
            switch (direction) {
                case IN:
                    return this.inMap.get(id);
                case OUT:
                    return this.outMap.get(id);
            }
            throw new IllegalArgumentException();
        }

        @Override
        public int fromPacketClass(Class<? extends MCPacket> packetClass) {
            int id = this.inMap.inverse().getOrDefault(packetClass, -1);
            if (id != -1) {
                return id;
            }
            id = this.outMap.inverse().getOrDefault(packetClass, -1);
            return id;
        }

    }

}
