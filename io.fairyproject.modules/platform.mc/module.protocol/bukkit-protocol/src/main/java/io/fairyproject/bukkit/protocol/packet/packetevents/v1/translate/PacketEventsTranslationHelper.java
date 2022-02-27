package io.fairyproject.bukkit.protocol.packet.packetevents.v1.translate;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.fairyproject.bukkit.protocol.PacketBuilder;
import io.fairyproject.bukkit.protocol.PacketFactoryCreator;
import io.fairyproject.bukkit.protocol.PacketFactoryWrapper;
import io.fairyproject.bukkit.protocol.PacketMap;
import io.fairyproject.bukkit.protocol.packet.packetevents.v1.mapping.PacketEventsV1Mapping;
import io.fairyproject.bukkit.protocol.packet.packetevents.v1.netty.PacketEventsChannel;
import io.fairyproject.bukkit.protocol.packet.packetevents.v1.wrappers.*;
import io.fairyproject.bukkit.protocol.packet.packetevents.v1.PacketEventWrapper;
import io.fairyproject.bukkit.protocol.packet.packetevents.v1.wrappers.CPacketEventsRotation;
import io.fairyproject.bukkit.reflection.wrapper.ConstructorWrapper;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.mcp.Direction;
import io.fairyproject.mc.mcp.Hand;
import io.fairyproject.mc.mcp.PlayerAction;
import io.fairyproject.mc.protocol.packet.Packet;
import io.fairyproject.mc.protocol.translate.Translator;
import io.fairyproject.mc.util.Vec3f;
import io.fairyproject.mc.util.Vec3i;
import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.event.impl.PacketPlayReceiveEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.custompayload.WrappedPacketInCustomPayload;
import io.github.retrooper.packetevents.packetwrappers.play.in.entityaction.WrappedPacketInEntityAction;
import io.github.retrooper.packetevents.utils.vector.Vector3f;
import io.github.retrooper.packetevents.utils.vector.Vector3i;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class PacketEventsTranslationHelper {
    public final Translator<io.netty.channel.Channel, PacketEventsChannel> CHANNEL = new Translator<io.netty.channel.Channel, PacketEventsChannel>() {
        @Override
        public PacketEventsChannel transform(io.netty.channel.Channel from) {
            return new PacketEventsChannel(from);
        }
    };

    private final PacketEventsV1Mapping mappings = new PacketEventsV1Mapping();

    public final Translator<PacketPlayReceiveEvent, Packet> PACKET = new Translator<PacketPlayReceiveEvent, Packet>() {
        @Override
        public Packet transform(PacketPlayReceiveEvent from) {
            MCPlayer player = MCPlayer.from(from.getPlayer());
            mappings.wrap(player, from.getPacketId(), from.getNMSPacket());
            val packetId = from.getPacketId();

            final PacketEventsTranslationHelper.PacketGenerator<?, ?> generator = generators.get(packetId);

            if (generator == null) {
                return null;
            }

            return generator.build(from.getNMSPacket(),player));
        }
    };
    
    public final Translator<io.github.retrooper.packetevents.utils.player.Hand, Hand> HAND = new Translator<io.github.retrooper.packetevents.utils.player.Hand, Hand>() {
        @Override
        public Hand transform(io.github.retrooper.packetevents.utils.player.Hand from) {
            return Hand.values()[from.ordinal()];
        }
    };

    public final Translator<io.github.retrooper.packetevents.utils.player.Direction, Direction> DIRECTION = new Translator<io.github.retrooper.packetevents.utils.player.Direction, Direction>() {
        @Override
        public Direction transform(io.github.retrooper.packetevents.utils.player.Direction from) {
            return Direction.getDirection(from.getFaceValue());
        }
    };

    public final Translator<WrappedPacketInEntityAction.PlayerAction, PlayerAction> PLAYER_ACTION = new Translator<WrappedPacketInEntityAction.PlayerAction, PlayerAction>() {
        @Override
        public PlayerAction transform(WrappedPacketInEntityAction.PlayerAction from) {
            return PlayerAction.values()[from.ordinal()];
        }
    };

    public final Translator<Vector3f, Vec3f> VECTOR_3F = new Translator<Vector3f, Vec3f>() {
        @Override
        public Vec3f transform(Vector3f from) {
            return new Vec3f(from.getX(), from.getY(), from.getZ());
        }
    };

    public final Translator<Vector3i, Vec3i> VECTOR_3I = new Translator<Vector3i, Vec3i>() {
        @Override
        public Vec3i transform(Vector3i from) {
            return new Vec3i(from.getX(), from.getY(), from.getZ());
        }
    };
}
