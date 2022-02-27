package io.fairyproject.bukkit.protocol.packet.artemispacketapi.translate;

import io.fairyproject.bukkit.protocol.packet.artemispacketapi.netty.ArtemisChannel;
import io.fairyproject.bukkit.protocol.packet.artemispacketapi.wrappers.*;
import io.fairyproject.mc.mcp.Direction;
import io.fairyproject.mc.mcp.Hand;
import io.fairyproject.mc.mcp.PlayerAction;
import io.fairyproject.mc.protocol.packet.Packet;
import io.fairyproject.mc.protocol.packet.translate.Translator;
import ac.artemis.packet.profile.Profile;
import ac.artemis.packet.spigot.wrappers.GPacket;
import cc.ghast.packet.nms.EnumDirection;
import cc.ghast.packet.profile.ArtemisProfile;
import cc.ghast.packet.utils.Pair;
import cc.ghast.packet.wrapper.bukkit.BlockPosition;
import cc.ghast.packet.wrapper.bukkit.Vector3D;
import cc.ghast.packet.wrapper.mc.PlayerEnums;
import cc.ghast.packet.wrapper.packet.play.client.*;
import io.fairyproject.mc.util.Vec3f;
import io.fairyproject.mc.util.Vec3i;
import io.netty.channel.Channel;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@UtilityClass
public class ArtemisPacketTranslators {
    public final Translator<Channel, ArtemisChannel> CHANNEL = new Translator<Channel, ArtemisChannel>() {
        @Override
        public ArtemisChannel transform(Channel from) {
            return new ArtemisChannel(from);
        }
    };

    private final Map<Class<? extends GPacket>, TranslatorFunction<GPacket>> packetTranslatorMap = new HashMap<>();

    void initPacket() {
        packetTranslatorMap.put(GPacketPlayClientAbilities.class, new TranslatorFunction<GPacket>() {
            @Override
            public Packet createPacket(GPacket wrapper, io.fairyproject.mc.protocol.netty.Channel channel) {
                return new CPacketArtemisAbilities((GPacketPlayClientAbilities) wrapper, channel);
            }
        });
        packetTranslatorMap.put(GPacketPlayClientBlockPlace.class, new TranslatorFunction<GPacket>() {
            @Override
            public Packet createPacket(GPacket wrapper, io.fairyproject.mc.protocol.netty.Channel profile) {
                return new CPacketArtemisBlockPlace((GPacketPlayClientBlockPlace) wrapper, profile);
            }
        });
        packetTranslatorMap.put(GPacketPlayClientChat.class, new TranslatorFunction<GPacket>() {
            @Override
            public Packet createPacket(GPacket wrapper, io.fairyproject.mc.protocol.netty.Channel profile) {
                return new CPacketArtemisChat((GPacketPlayClientChat) wrapper, profile);
            }
        });
        packetTranslatorMap.put(GPacketPlayClientCustomPayload.class, new TranslatorFunction<GPacket>() {
            @Override
            public Packet createPacket(GPacket wrapper, io.fairyproject.mc.protocol.netty.Channel profile) {
                return new CPacketArtemisCustomPayload((GPacketPlayClientCustomPayload) wrapper, profile);
            }
        });
        packetTranslatorMap.put(GPacketPlayClientEntityAction.class, new TranslatorFunction<GPacket>() {
            @Override
            public Packet createPacket(GPacket wrapper, io.fairyproject.mc.protocol.netty.Channel profile) {
                return new CPacketArtemisEntityAction((GPacketPlayClientEntityAction) wrapper, profile);
            }
        });
        packetTranslatorMap.put(GPacketPlayClientFlying.class, new TranslatorFunction<GPacket>() {
            @Override
            public Packet createPacket(GPacket wrapper, io.fairyproject.mc.protocol.netty.Channel profile) {
                return new CPacketArtemisFlying((GPacketPlayClientFlying) wrapper, profile);
            }
        });
        packetTranslatorMap.put(GPacketPlayClientHeldItemSlot.class, new TranslatorFunction<GPacket>() {
            @Override
            public Packet createPacket(GPacket wrapper, io.fairyproject.mc.protocol.netty.Channel profile) {
                return new CPacketArtemisHeldItemSlot((GPacketPlayClientHeldItemSlot) wrapper, profile);
            }
        });
        packetTranslatorMap.put(GPacketPlayClientPosition.class, new TranslatorFunction<GPacket>() {
            @Override
            public Packet createPacket(GPacket wrapper, io.fairyproject.mc.protocol.netty.Channel profile) {
                return new CPacketArtemisHeldItemSlot((GPacketPlayClientHeldItemSlot) wrapper, profile);
            }
        });
        packetTranslatorMap.put(GPacketPlayClientPositionLook.class, new TranslatorFunction<GPacket>() {
            @Override
            public Packet createPacket(GPacket wrapper, io.fairyproject.mc.protocol.netty.Channel profile) {
                return new CPacketArtemisPositionRotation((GPacketPlayClientPositionLook) wrapper, profile);
            }
        });
        packetTranslatorMap.put(GPacketPlayClientLook.class, new TranslatorFunction<GPacket>() {
            @Override
            public Packet createPacket(GPacket wrapper, io.fairyproject.mc.protocol.netty.Channel profile) {
                return new CPacketArtemisRotation((GPacketPlayClientLook) wrapper, profile);
            }
        });
        packetTranslatorMap.put(GPacketPlayClientSetCreativeSlot.class, new TranslatorFunction<GPacket>() {
            @Override
            public Packet createPacket(GPacket wrapper, io.fairyproject.mc.protocol.netty.Channel profile) {
                return new CPacketArtemisSetCreativeSlot((GPacketPlayClientSetCreativeSlot) wrapper, profile);
            }
        });
        packetTranslatorMap.put(GPacketPlayClientSpectate.class, new TranslatorFunction<GPacket>() {
            @Override
            public Packet createPacket(GPacket wrapper, io.fairyproject.mc.protocol.netty.Channel profile) {
                return new CPacketArtemisSpectate((GPacketPlayClientSpectate) wrapper, profile);
            }
        });
        packetTranslatorMap.put(GPacketPlayClientWindowClick.class, new TranslatorFunction<GPacket>() {
            @Override
            public Packet createPacket(GPacket wrapper, io.fairyproject.mc.protocol.netty.Channel profile) {
                return new CPacketArtemisWindowClick((GPacketPlayClientWindowClick) wrapper, profile);
            }
        });
    }

    interface TranslatorFunction<T> extends Function<Pair<Profile, T>, Packet> {
        @Override
        default Packet apply(Pair<Profile, T> profilePair) {
            final ArtemisProfile profile = (ArtemisProfile) profilePair.getK();
            return createPacket(profilePair.getV(), CHANNEL.transform((Channel) profile.getChannel()));
        }

        Packet createPacket(final T wrapper, final io.fairyproject.mc.protocol.netty.Channel channel);
    }

    public final Translator<Pair<Profile, GPacket>, Packet> PACKET = new Translator<Pair<Profile, GPacket>, Packet>() {
        @Override
        public Packet transform(Pair<Profile, GPacket> from) {
            val player = from.getV().getPlayer();
            val channel = (Channel) ((ArtemisProfile) from.getK()).getChannel();

            final TranslatorFunction<GPacket> translator = packetTranslatorMap.get(from.getV().getClass());

            if (translator == null)
                return null;

            return translator.apply(from);
        }
    };
    
    public final Translator<PlayerEnums.Hand, Hand> HAND = new Translator<PlayerEnums.Hand, Hand>() {
        @Override
        public Hand transform(PlayerEnums.Hand from) {
            return Hand.values()[from.ordinal()];
        }
    };

    public final Translator<EnumDirection, Direction> DIRECTION = new Translator<EnumDirection, Direction>() {
        @Override
        public Direction transform(EnumDirection from) {
            return Direction.getDirection(from.ordinal());
        }
    };

    public final Translator<PlayerEnums.PlayerAction, PlayerAction> PLAYER_ACTION = new Translator<PlayerEnums.PlayerAction, PlayerAction>() {
        @Override
        public PlayerAction transform(PlayerEnums.PlayerAction from) {
            return PlayerAction.values()[from.ordinal()];
        }
    };

    public final Translator<Vector3D, Vec3f> VECTOR_3D = new Translator<Vector3D, Vec3f>() {
        @Override
        public Vec3f transform(Vector3D from) {
            // Don't question it, I'm (Ghast) brutally incompetent
            return new Vec3f(from.getX(), from.getY(), from.getZ());
        }
    };

    public final Translator<BlockPosition, Vec3i> VECTOR_3I = new Translator<BlockPosition, Vec3i>() {
        @Override
        public Vec3i transform(BlockPosition from) {
            return new Vec3i(from.getX(), from.getY(), from.getZ());
        }
    };
}
