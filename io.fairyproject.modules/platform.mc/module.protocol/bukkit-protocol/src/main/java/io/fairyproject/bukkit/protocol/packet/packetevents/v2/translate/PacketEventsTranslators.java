package io.fairyproject.bukkit.protocol.packet.packetevents.v2.translate;

import io.fairyproject.bukkit.protocol.packet.packetevents.v1.netty.PacketEventsChannel;
import io.fairyproject.bukkit.protocol.packet.packetevents.v1.wrappers.*;
import io.fairyproject.mc.mcp.Direction;
import io.fairyproject.mc.mcp.Hand;
import io.fairyproject.mc.mcp.PlayerAction;
import io.fairyproject.mc.protocol.packet.Packet;
import io.fairyproject.mc.protocol.packet.translate.Translator;
import io.fairyproject.mc.util.Vec3f;
import io.fairyproject.mc.util.Vec3i;
import io.netty.channel.Channel;
import lombok.val;

public class PacketEventsTranslators {
    public static final Translator<Channel, PacketEventsChannel> CHANNEL = new Translator<Channel, PacketEventsChannel>() {
        @Override
        public PacketEventsChannel transform(Channel from) {
            return new PacketEventsChannel(from);
        }
    };

    public static final Translator<PacketPlayReceiveEvent, Packet> PACKET = new Translator<PacketPlayReceiveEvent, Packet>() {
        @Override
        public Packet transform(PacketPlayReceiveEvent from) {
            val player = from.getPlayer();
            val channel = (Channel) PacketEvents.get().getPlayerUtils().getChannel(player);
            val nmsPacket = from.getNMSPacket();
            val packetId = from.getPacketId();

            final PacketEventsChannel changedChannel = CHANNEL.transform(channel);

            switch (packetId) {
                case PacketType.Play.Client.CUSTOM_PAYLOAD:
                    val customPayload = new WrappedPacketInCustomPayload(nmsPacket);
                    return new CPacketEventsCustomPayload(customPayload, changedChannel);

                case PacketType.Play.Client.BLOCK_PLACE:
                    val blockPlace = new WrappedPacketInBlockPlace(nmsPacket);
                    return new CPacketEventsBlockPlace(blockPlace, changedChannel);

                case PacketType.Play.Client.WINDOW_CLICK:
                    val windowCLick = new WrappedPacketInWindowClick(nmsPacket);
                    return new CPacketEventsWindowClick(windowCLick, changedChannel);

                case PacketType.Play.Client.SET_CREATIVE_SLOT:
                    val creativeSlot = new WrappedPacketInSetCreativeSlot(nmsPacket);
                    return new CPacketEventsSetCreativeSlot(creativeSlot, changedChannel);

                case PacketType.Play.Client.ENTITY_ACTION:
                    val entityAction = new WrappedPacketInEntityAction(nmsPacket);
                    return new CPacketEventsEntityAction(entityAction, changedChannel);

                case PacketType.Play.Client.ABILITIES:
                    val abilities = new WrappedPacketInAbilities(nmsPacket);
                    return new CPacketEventsAbilities(abilities, changedChannel);

                case PacketType.Play.Client.HELD_ITEM_SLOT:
                    val heldItemSlot = new WrappedPacketInHeldItemSlot(nmsPacket);
                    return new CPacketEventsHeldItemSlot(heldItemSlot, changedChannel);

                case PacketType.Play.Client.CHAT:
                    val chat = new WrappedPacketInChat(nmsPacket);
                    return new CPacketEventsChat(chat, changedChannel);
                case PacketType.Play.Client.TAB_COMPLETE:
                    val tabComplete = new WrappedPacketInTabComplete(nmsPacket);
                    return new CPacketEventsTabComplete(tabComplete, changedChannel);

                case PacketType.Play.Client.SPECTATE:
                    val spectate = new WrappedPacketInSpectate(nmsPacket);
                    return new CPacketEventsSpectate(spectate, changedChannel);

                default:
                    break;
            }

            if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
                val flying = new WrappedPacketInFlying(nmsPacket);

                if (flying.isMoving() && flying.isRotating()) {
                    return new CPacketEventsPositionRotation(flying, changedChannel);
                } else if (flying.isMoving()) {
                    return new CPacketEventsPosition(flying, changedChannel);
                } else if (flying.isRotating()) {
                    return new CPacketEventsRotation(flying, changedChannel);
                } else {
                    return new CPacketEventsFlying(flying, changedChannel);
                }
            }

            return null;
        }
    };
    
    public static final Translator<io.github.retrooper.packetevents.utils.player.Hand, Hand> HAND = new Translator<io.github.retrooper.packetevents.utils.player.Hand, Hand>() {
        @Override
        public Hand transform(io.github.retrooper.packetevents.utils.player.Hand from) {
            return Hand.values()[from.ordinal()];
        }
    };

    public static final Translator<io.github.retrooper.packetevents.utils.player.Direction, Direction> DIRECTION = new Translator<io.github.retrooper.packetevents.utils.player.Direction, Direction>() {
        @Override
        public Direction transform(io.github.retrooper.packetevents.utils.player.Direction from) {
            return Direction.getDirection(from.getFaceValue());
        }
    };

    public static final Translator<WrappedPacketInEntityAction.PlayerAction, PlayerAction> PLAYER_ACTION = new Translator<WrappedPacketInEntityAction.PlayerAction, PlayerAction>() {
        @Override
        public PlayerAction transform(WrappedPacketInEntityAction.PlayerAction from) {
            return PlayerAction.values()[from.ordinal()];
        }
    };

    public static final Translator<Vector3f, Vec3f> VECTOR_3F = new Translator<Vector3f, Vec3f>() {
        @Override
        public Vec3f transform(Vector3f from) {
            return new Vec3f(from.getX(), from.getY(), from.getZ());
        }
    };

    public static final Translator<Vector3i, Vec3i> VECTOR_3I = new Translator<Vector3i, Vec3i>() {
        @Override
        public Vec3i transform(Vector3i from) {
            return new Vec3i(from.getX(), from.getY(), from.getZ());
        }
    };
}
