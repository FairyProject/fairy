package io.fairyproject.mc.protocol.packet;

import io.fairyproject.mc.protocol.MCPacket;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.protocol.MCVersion;
import io.fairyproject.mc.protocol.item.*;
import io.fairyproject.mc.protocol.netty.FriendlyByteBuf;
import lombok.*;
import net.kyori.adventure.text.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PacketPlay {

    public static class Out {

        @Getter @Setter @Builder
        public static class ScoreboardScore implements MCPacket {

            private String owner;
            private String objectiveName;
            private int score;
            private ScoreAction action;

            @Override
            public void read(FriendlyByteBuf byteBuf) {
                this.owner = byteBuf.readUtf();
                this.action = byteBuf.readEnum(ScoreAction.class);
                String string = byteBuf.readUtf();
                this.objectiveName = Objects.equals(string, "") ? null : string;
                if (this.action != ScoreAction.REMOVE) {
                    this.score = byteBuf.readVarInt();
                } else {
                    this.score = 0;
                }
            }

            @Override
            public void write(FriendlyByteBuf byteBuf) {
                if (action != ScoreAction.REMOVE && this.objectiveName == null) {
                    throw new IllegalArgumentException("Need an objective name.");
                }
                byteBuf.writeUtf(this.owner);
                byteBuf.writeEnum(this.action);
                byteBuf.writeUtf(this.objectiveName == null ? "" : this.objectiveName);
                if (this.action != ScoreAction.REMOVE) {
                    byteBuf.writeVarInt(this.score);
                }
            }
        }

        @Getter @Setter @Builder
        public static class ScoreboardDisplayObjective implements MCPacket {

            private ObjectiveDisplaySlot displaySlot;
            private String objectiveName;

            @Override
            public void read(FriendlyByteBuf byteBuf) {
                this.displaySlot = ObjectiveDisplaySlot.IDS.value((int) byteBuf.readByte());
                this.objectiveName = byteBuf.readUtf();
            }

            @Override
            public void write(FriendlyByteBuf byteBuf) {
                byteBuf.writeByte(this.displaySlot.getSerializeId());
                byteBuf.writeUtf(this.objectiveName);
            }
        }

        @Getter @Setter @Builder
        public static class ScoreboardObjective implements MCPacket {
            private String objectiveName;
            private Component displayName;
            private ObjectiveRenderType renderType;
            private int method;

            @Override
            public void read(FriendlyByteBuf byteBuf) {
                this.objectiveName = byteBuf.readUtf();
                this.method = byteBuf.readByte();
                if (this.method != 0 && this.method != 2) {
                    this.displayName = Component.empty();
                    this.renderType = ObjectiveRenderType.INTEGER;
                } else {
                    this.displayName = byteBuf.readComponent(MCProtocol.INSTANCE.version().below(MCVersion.V1_13));
                    this.renderType = byteBuf.readEnum(ObjectiveRenderType.class);
                }
            }

            @Override
            public void write(FriendlyByteBuf byteBuf) {
                byteBuf.writeUtf(this.objectiveName);
                byteBuf.writeByte(this.method);
                if (method == 0 || method == 2) {
                    byteBuf.writeComponent(this.displayName, MCProtocol.INSTANCE.version().below(MCVersion.V1_13));
                    byteBuf.writeEnum(this.renderType);
                }
            }
        }

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
                @Builder.Default
                private Component displayName = Component.empty();
                @Builder.Default
                private Component playerPrefix = Component.empty();
                @Builder.Default
                private Component playerSuffix = Component.empty();
                @Builder.Default
                private NameTagVisibility nametagVisibility = NameTagVisibility.ALWAYS;
                @Builder.Default
                private CollisionRule collisionRule = CollisionRule.ALWAYS;
                @Builder.Default
                private ChatFormatting color = ChatFormatting.BLACK;
                private int options;

                public Parameters(FriendlyByteBuf buf) {
                    switch (MCProtocol.INSTANCE.version()) {
                        case V1_7:
                            this.displayName = buf.readComponent(true);
                            this.playerPrefix = buf.readComponent(true);
                            this.playerSuffix = buf.readComponent(true);
                            this.options = buf.readByte();
                            this.color = buf.readEnum(ChatFormatting.class);
                            this.nametagVisibility = NameTagVisibility.ALWAYS;
                            this.collisionRule = CollisionRule.ALWAYS;
                            break;
                        case V1_8:
                            this.displayName = buf.readComponent(true);
                            this.playerPrefix = buf.readComponent(true);
                            this.playerSuffix = buf.readComponent(true);
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
                            this.playerPrefix = buf.readComponent(MCProtocol.INSTANCE.version().below(MCVersion.V1_13));
                            this.playerSuffix = buf.readComponent(MCProtocol.INSTANCE.version().below(MCVersion.V1_13));
                            break;
                    }
                }

                public void write(FriendlyByteBuf buf) {
                    buf.writeComponent(this.displayName, MCProtocol.INSTANCE.version().below(MCVersion.V1_13));
                    switch (MCProtocol.INSTANCE.version()) {
                        case V1_7:
                            buf.writeComponent(this.playerPrefix, true);
                            buf.writeComponent(this.playerSuffix, true);
                            buf.writeByte(this.options);
                            buf.writeEnum(this.color);
                            break;
                        case V1_8:
                            buf.writeComponent(this.playerPrefix, true);
                            buf.writeComponent(this.playerSuffix, true);
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
                            buf.writeComponent(this.playerPrefix, MCProtocol.INSTANCE.version().below(MCVersion.V1_13));
                            buf.writeComponent(this.playerSuffix, MCProtocol.INSTANCE.version().below(MCVersion.V1_13));
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
                if (MCProtocol.INSTANCE.version().below(MCVersion.V1_17)) {
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
                if (MCProtocol.INSTANCE.version().below(MCVersion.V1_17)) {
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
                if (MCProtocol.INSTANCE.version().isOrAbove(MCVersion.V1_17)) {
                    this.resetTimes = byteBuf.readBoolean();
                }
            }

            @Override
            public void write(FriendlyByteBuf byteBuf) {
                if (MCProtocol.INSTANCE.version().below(MCVersion.V1_17)) {
                    byteBuf.writeVarInt(resetTimes ? 4 : 3);
                } else {
                    byteBuf.writeBoolean(this.resetTimes);
                }
            }
        }

        @Getter @Setter @Builder
        public static class WorldBorderInitialize implements MCPacket {

            private double x;
            private double z;
            private double oldDiameter;
            private double newDiameter;

            private long speed;

            private int portalTeleportBoundary;
            private int warningBlocks;
            private int warningTime;

            @Override
            public void read(FriendlyByteBuf byteBuf) {
                this.x = byteBuf.readDouble();
                this.z = byteBuf.readDouble();
                this.oldDiameter = byteBuf.readDouble();
                this.newDiameter = byteBuf.readDouble();

                this.speed = byteBuf.readVarLong();

                this.portalTeleportBoundary = byteBuf.readVarInt();
                this.warningBlocks = byteBuf.readVarInt();
                this.warningTime = byteBuf.readVarInt();
            }

            @Override
            public void write(FriendlyByteBuf byteBuf) {
                byteBuf.writeDouble(this.x);
                byteBuf.writeDouble(this.z);
                byteBuf.writeDouble(this.oldDiameter);
                byteBuf.writeDouble(this.newDiameter);

                byteBuf.writeVarLong(this.speed);

                byteBuf.writeVarInt(this.portalTeleportBoundary);
                byteBuf.writeVarInt(this.warningBlocks);
                byteBuf.writeVarInt(this.warningTime);
            }
        }

    }

}
