package org.fairy.mc.protocol.packet;

import lombok.experimental.UtilityClass;
import org.fairy.mc.protocol.MCPacket;
import org.fairy.mc.protocol.netty.FriendlyByteBuf;

@UtilityClass
public class PacketPlay {

    @UtilityClass
    public class Out {

        public class TitleSetText implements MCPacket {

            @Override
            public void read(FriendlyByteBuf byteBuf) {

            }

            @Override
            public void write(FriendlyByteBuf byteBuf) {

            }

        }

    }

}
