package io.fairyproject.mc.protocol.item;

import io.fairyproject.mc.GameMode;
import io.fairyproject.mc.MCGameProfile;
import io.fairyproject.mc.protocol.netty.FriendlyByteBuf;
import io.fairyproject.mc.util.Property;
import lombok.Getter;
import net.kyori.adventure.text.Component;

public enum PlayerInfoAction {
    ADD_PLAYER(0) {
        @Override
        public PlayerInfoData read(FriendlyByteBuf byteBuf) {
            MCGameProfile gameProfile = MCGameProfile.create(byteBuf.readUUID(), byteBuf.readUtf(16));
            byteBuf.readWithCount(buf -> {
                String s = buf.readUtf();
                String s2 = buf.readUtf();
                if (buf.readBoolean()) {
                    String s3 = buf.readUtf();
                    gameProfile.setProperty(new Property(s, s2, s3));
                } else {
                    gameProfile.setProperty(new Property(s, s2));
                }
            });
            GameMode gameMode = GameMode.getByValue(byteBuf.readVarInt());
            int i = byteBuf.readVarInt();
            Component component = byteBuf.readBoolean() ? byteBuf.readComponent() : null;
            return new PlayerInfoData(i, gameProfile, gameMode, component);
        }

        @Override
        public void write(FriendlyByteBuf byteBuf, PlayerInfoData playerInfoData) {
            byteBuf.writeUUID(playerInfoData.getGameProfile().getUuid());
            byteBuf.writeUtf(playerInfoData.getGameProfile().getName());
            byteBuf.writeCollection(playerInfoData.getGameProfile().getProperties(), (buf, property) -> {
                buf.writeUtf(property.getName());
                buf.writeUtf(property.getValue());
                if (property.hasSignature()) {
                    buf.writeBoolean(true);
                    buf.writeUtf(property.getSignature());
                } else {
                    buf.writeBoolean(false);
                }
            });
            byteBuf.writeVarInt(playerInfoData.getGameMode().getValue());
            byteBuf.writeVarInt(playerInfoData.getPing());
            if (playerInfoData.getComponent() != null) {
                byteBuf.writeBoolean(true);
                byteBuf.writeComponent(playerInfoData.getComponent());
            } else {
                byteBuf.writeBoolean(false);
            }
        }
    },
    UPDATE_GAME_MODE(1) {
        @Override
        public PlayerInfoData read(FriendlyByteBuf byteBuf) {
            MCGameProfile gameProfile = MCGameProfile.create(byteBuf.readUUID(), null);
            GameMode gameMode = GameMode.getByValue(byteBuf.readVarInt());
            return new PlayerInfoData(0, gameProfile, gameMode, null);
        }
        @Override
        public void write(FriendlyByteBuf byteBuf, PlayerInfoData playerInfoData) {
            byteBuf.writeUUID(playerInfoData.getGameProfile().getUuid());
            byteBuf.writeVarInt(playerInfoData.getGameMode().getValue());
        }
    },
    UPDATE_LATENCY(2) {
        @Override
        public PlayerInfoData read(FriendlyByteBuf byteBuf) {
            MCGameProfile gameProfile = MCGameProfile.create(byteBuf.readUUID(), null);
            int ping = byteBuf.readVarInt();
            return new PlayerInfoData(ping, gameProfile, null, null);
        }
        @Override
        public void write(FriendlyByteBuf byteBuf, PlayerInfoData playerInfoData) {
            byteBuf.writeUUID(playerInfoData.getGameProfile().getUuid());
            byteBuf.writeVarInt(playerInfoData.getPing());
        }
    },
    UPDATE_DISPLAY_NAME(3) {
        @Override
        public PlayerInfoData read(FriendlyByteBuf byteBuf) {
            MCGameProfile gameProfile = MCGameProfile.create(byteBuf.readUUID(), null);
            Component component = byteBuf.readBoolean() ? byteBuf.readComponent() : null;
            return new PlayerInfoData(0, gameProfile, null, component);
        }
        @Override
        public void write(FriendlyByteBuf byteBuf, PlayerInfoData playerInfoData) {
            byteBuf.writeUUID(playerInfoData.getGameProfile().getUuid());
            if (playerInfoData.getComponent() != null) {
                byteBuf.writeBoolean(true);
                byteBuf.writeComponent(playerInfoData.getComponent());
            } else {
                byteBuf.writeBoolean(false);
            }
        }
    },
    REMOVE_PLAYER(4) {
        @Override
        public PlayerInfoData read(FriendlyByteBuf byteBuf) {
            MCGameProfile gameProfile = MCGameProfile.create(byteBuf.readUUID(), null);
            return new PlayerInfoData(0, gameProfile, null, null);
        }
        @Override
        public void write(FriendlyByteBuf byteBuf, PlayerInfoData playerInfoData) {
            byteBuf.writeUUID(playerInfoData.getGameProfile().getUuid());
        }
    };

    @Getter
    private final int id;

    PlayerInfoAction(int id) {
        this.id = id;
    }

    public static PlayerInfoAction getById(int id) {
        for (PlayerInfoAction action : PlayerInfoAction.values()) {
            if (action.getId() == id) {
                return action;
            }
        }
        return null;
    }

    public PlayerInfoData read(FriendlyByteBuf byteBuf) {
        throw new UnsupportedOperationException();
    }

    public void write(FriendlyByteBuf byteBuf, PlayerInfoData playerInfoData) {
        throw new UnsupportedOperationException();
    }
}
