package io.fairyproject.mc.protocol.packet;

import io.fairyproject.mc.protocol.MCPacket;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.protocol.MCVersion;
import io.fairyproject.mc.protocol.netty.FriendlyByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;

@UtilityClass
public class PacketPlay {

    @UtilityClass
    public class Out {

        @Getter
        @Setter
        public class Title implements MCPacket {
            private Component component;
            @Override
            public void read(FriendlyByteBuf byteBuf) {
                this.component = byteBuf.readComponent();
            }
            @Override
            public void write(FriendlyByteBuf byteBuf) {
                if (MCProtocol.INSTANCE.getProtocolMapping().getVersion().below(MCVersion.V1_17)) {
                    byteBuf.writeVarInt(0);
                }
                byteBuf.writeComponent(component);
            }
        }

        @Getter
        @Setter
        public class SubTitle implements MCPacket {
            private Component component;
            @Override
            public void read(FriendlyByteBuf byteBuf) {
                this.component = byteBuf.readComponent();
            }
            @Override
            public void write(FriendlyByteBuf byteBuf) {
                if (MCProtocol.INSTANCE.getProtocolMapping().getVersion().below(MCVersion.V1_17)) {
                    byteBuf.writeVarInt(1);
                }
                byteBuf.writeComponent(component);
            }
        }

        @Getter
        @Setter
        public class TitleTimes implements MCPacket {
            private int fadeIn;
            private int stay;
            private int fadeOut;
            @Override
            public void read(FriendlyByteBuf byteBuf) {
                this.fadeIn = byteBuf.readInt();
                this.stay = byteBuf.readInt();
                this.fadeOut = byteBuf.readInt();
            }
            @Override
            public void write(FriendlyByteBuf byteBuf) {
                if (MCProtocol.INSTANCE.getProtocolMapping().getVersion().below(MCVersion.V1_17)) {
                    byteBuf.writeVarInt(2);
                }
                byteBuf.writeInt(this.fadeIn);
                byteBuf.writeInt(this.stay);
                byteBuf.writeInt(this.fadeOut);
            }
        }

        @Getter
        @Setter
        public class TitleClear implements MCPacket {
            private boolean resetTimes;
            @Override
            public void read(FriendlyByteBuf byteBuf) {
                if (MCProtocol.INSTANCE.getProtocolMapping().getVersion().isOrAbove(MCVersion.V1_17)) {
                    this.resetTimes = byteBuf.readBoolean();
                }
            }
            @Override
            public void write(FriendlyByteBuf byteBuf) {
                if (MCProtocol.INSTANCE.getProtocolMapping().getVersion().below(MCVersion.V1_17)) {
                    byteBuf.writeVarInt(resetTimes ? 4 : 3);
                } else {
                    byteBuf.writeBoolean(this.resetTimes);
                }
            }
        }

    }

}
