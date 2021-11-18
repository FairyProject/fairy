package io.fairyproject.mc.protocol.packet;

import io.fairyproject.mc.protocol.MCPacket;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.protocol.MCVersion;
import io.fairyproject.mc.protocol.item.*;
import io.fairyproject.mc.protocol.netty.FriendlyByteBuf;
import lombok.*;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PacketPlay {

    public static class Out {

        @Getter @Setter @Builder
        public static class Tablist implements MCPacket {
            private Component header, footer;
            @Override
            public void read(FriendlyByteBuf byteBuf) {
                this.header = byteBuf.readComponent();
                this.footer = byteBuf.readComponent();
            }

            @Override
            public void write(FriendlyByteBuf byteBuf) {
                byteBuf.writeComponent(this.header);
                byteBuf.writeComponent(this.footer);
            }
        }

        @Getter @Setter @Builder
        public static class PlayerInfo implements MCPacket {
            private PlayerInfoAction action;
            @Singular
            private List<PlayerInfoData> entries;
            @Override
            public void read(FriendlyByteBuf byteBuf) {
                this.action = byteBuf.readEnum(PlayerInfoAction.class);
                this.entries = byteBuf.readList(this.action::read);
            }
            @Override
            public void write(FriendlyByteBuf byteBuf) {
                byteBuf.writeEnum(this.action);
                byteBuf.writeCollection(this.entries, this.action::write);
            }
        }

        @Getter @Setter @Builder
        public static class ScoreboardTeam implements MCPacket {
            private String name;
            private TeamAction teamAction;
            private NameTagVisibility nameTagVisibility;
            @Singular
            private List<String> players;
            private Optional<Parameters> parameters;

            @Override
            public void read(FriendlyByteBuf byteBuf) {
                this.name = byteBuf.readUtf();
                this.teamAction = TeamAction.getById(byteBuf.readByte());
                if (this.teamAction.isHasParameters()) {
                    this.parameters = Optional.of(new Parameters(byteBuf));
                } else {
                    this.parameters = Optional.empty();
                }

                if (this.teamAction.isHasPlayers()) {
                    this.players = byteBuf.readList(FriendlyByteBuf::readUtf);
                } else {
                    this.players = Collections.emptyList();
                }
            }
            @Override
            public void write(FriendlyByteBuf byteBuf) {
                byteBuf.writeUtf(this.name);
                byteBuf.writeByte(this.teamAction.getId());
                if (this.teamAction.isHasParameters()) {
                    this.parameters.orElseThrow(() -> new IllegalStateException("Parameters not present, but method is " + this.teamAction.name())).write(byteBuf);
                }

                if (this.teamAction.isHasPlayers()) {
                    byteBuf.writeCollection(this.players, FriendlyByteBuf::writeUtf);
                }
            }
            @NoArgsConstructor @AllArgsConstructor @Getter @Setter @Builder
            public static class Parameters {
                private Component displayName;
                private Component playerPrefix;
                private Component playerSuffix;
                private NameTagVisibility nametagVisibility;
                private CollisionRule collisionRule;
                private ChatFormatting color;
                private int options;

                public Parameters(FriendlyByteBuf buf) {
                    switch (MCProtocol.INSTANCE.getProtocolMapping().getVersion()) {
                        case V1_7:
                            this.displayName = buf.readComponent();
                            this.playerPrefix = buf.readComponent();
                            this.playerSuffix = buf.readComponent();
                            this.options = buf.readByte();
                            this.color = buf.readEnum(ChatFormatting.class);
                            this.nametagVisibility = NameTagVisibility.ALWAYS;
                            this.collisionRule = CollisionRule.ALWAYS;
                            break;
                        case V1_8:
                            this.displayName = buf.readComponent();
                            this.playerPrefix = buf.readComponent();
                            this.playerSuffix = buf.readComponent();
                            this.options = buf.readByte();
                            this.nametagVisibility = NameTagVisibility.getByName(buf.readUtf(40));
                            this.collisionRule = CollisionRule.ALWAYS;
                            this.color = buf.readEnum(ChatFormatting.class);
                            break;
                        case V1_9:
                        default:
                            this.displayName = buf.readComponent();
                            this.options = buf.readByte();
                            this.nametagVisibility = NameTagVisibility.getByName(buf.readUtf(40));
                            this.collisionRule = CollisionRule.getByName(buf.readUtf(40));
                            this.color = buf.readEnum(ChatFormatting.class);
                            this.playerPrefix = buf.readComponent();
                            this.playerSuffix = buf.readComponent();
                            break;
                    }
                }

                public void write(FriendlyByteBuf buf) {
                    buf.writeComponent(this.displayName);
                    switch (MCProtocol.INSTANCE.getProtocolMapping().getVersion()) {
                        case V1_7:
                            buf.writeComponent(this.playerPrefix);
                            buf.writeComponent(this.playerSuffix);
                            buf.writeByte(this.options);
                            buf.writeEnum(this.color);
                            break;
                        case V1_8:
                            buf.writeComponent(this.playerPrefix);
                            buf.writeComponent(this.playerSuffix);
                            buf.writeByte(this.options);
                            buf.writeUtf(this.nametagVisibility.name);
                            buf.writeEnum(this.color);
                            break;
                        case V1_9:
                        default:
                            buf.writeByte(this.options);
                            buf.writeUtf(this.nametagVisibility.name);
                            buf.writeUtf(this.collisionRule.name);
                            buf.writeEnum(this.color);
                            buf.writeComponent(this.playerPrefix);
                            buf.writeComponent(this.playerSuffix);
                            break;
                    }
                }
            }
        }

        @Getter @Setter @Builder
        public static class Title implements MCPacket {
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

        @Getter @Setter @Builder
        public static class SubTitle implements MCPacket {
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

        @Getter @Setter @Builder
        public static class TitleTimes implements MCPacket {
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

        @Getter @Setter @Builder
        public static class TitleClear implements MCPacket {
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
