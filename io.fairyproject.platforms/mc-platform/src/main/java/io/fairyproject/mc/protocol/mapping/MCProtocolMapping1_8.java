package io.fairyproject.mc.protocol.mapping;

import io.fairyproject.mc.protocol.MCPacket;
import io.fairyproject.mc.protocol.packet.PacketDeserializer;
import io.fairyproject.mc.protocol.packet.PacketPlay;
import io.fairyproject.mc.protocol.MCVersion;

public class MCProtocolMapping1_8 extends MCProtocolMapping {

    public MCProtocolMapping1_8() {
        // Handshake
        this.registerProtocol(-1, new AbstractProtocol() {
            @Override
            public void init() {

            }
        });

        // Play
        this.registerProtocol(0, new AbstractProtocol() {
            @Override
            public void init() {
                // Titles
                this.registerOut(56, PacketPlay.Out.PlayerInfo.class);
                this.registerOut(62, PacketPlay.Out.ScoreboardTeam.class);
                this.registerOut(69, PacketPlay.Out.Title.class);
                this.registerOutDeserializer(69, (protocol, byteBuf, packetId, defaultType) -> {
                    final int titleAction = byteBuf.readVarInt();

                    Class<? extends MCPacket> packetClass;
                    switch (titleAction) {
                        case 0:
                            packetClass = PacketPlay.Out.Title.class;
                            break;
                        case 1:
                            packetClass = PacketPlay.Out.SubTitle.class;
                            break;
                        case 2:
                            packetClass = PacketPlay.Out.TitleTimes.class;
                            break;
                        case 3:
                        case 4:
                            packetClass = PacketPlay.Out.TitleClear.class;
                            break;
                        default:
                            throw new IllegalStateException("unrecognized title action.");
                    }

                    final MCPacket packet = PacketDeserializer.DEFAULT.deserialize(protocol, byteBuf, packetId, packetClass);
                    if (packet instanceof PacketPlay.Out.TitleClear) {
                        ((PacketPlay.Out.TitleClear) packet).setResetTimes(titleAction == 4);
                    }

                    return packet;
                });
                this.registerOut(71, PacketPlay.Out.Tablist.class);
            }
        });

        // Status
        this.registerProtocol(1, new AbstractProtocol() {
            @Override
            public void init() {

            }
        });

        // Login
        this.registerProtocol(2, new AbstractProtocol() {
            @Override
            public void init() {

            }
        });
    }

    @Override
    public MCVersion getVersion() {
        return MCVersion.V1_8;
    }
}
