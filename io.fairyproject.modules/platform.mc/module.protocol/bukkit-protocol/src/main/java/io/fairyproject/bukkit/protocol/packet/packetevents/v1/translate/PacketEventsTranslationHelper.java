package io.fairyproject.bukkit.protocol.packet.packetevents.v1.translate;

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

    private final Map<Byte, PacketEventsTranslationHelper.PacketGenerator<?, ?>> generators = new HashMap<>();

    static class PacketGenerator<T, W extends PacketEventWrapper<T>> {
        private final ConstructorWrapper<T> typeConstructor;
        private final ConstructorWrapper<W> wrapperConstructor;

        public PacketGenerator(Class<T> typeClass, Class<W> wrapperClass) {
            Constructor<T> _typeConstructor;
            try {
                _typeConstructor = typeClass.getDeclaredConstructor(NMSPacket.class);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Failed to create generator", e);
            }
            this.typeConstructor = new ConstructorWrapper<>(_typeConstructor);

            Constructor<W> _wrapperConstructor;
            try {
                _wrapperConstructor = wrapperClass.getDeclaredConstructor(typeClass, MCPlayer.class);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Failed to create generator", e);
            }
            this.wrapperConstructor = new ConstructorWrapper<>(_wrapperConstructor);

        }

        public W build(final NMSPacket event, final MCPlayer player) {
            final T instance = typeConstructor.newInstance(event);
            return wrapperConstructor.newInstance(instance, player);
        }

        static <T, W extends PacketEventWrapper<T>> PacketEventsTranslationHelper.PacketGenerator<T, W> create(Class<W> wrapperClass) {
            final Class<T> typeClass = (Class<T>) (ParameterizedType.class.cast(wrapperClass.getGenericSuperclass())).getActualTypeArguments()[0];
            return new PacketEventsTranslationHelper.PacketGenerator<>(typeClass, wrapperClass);
        }

        static  {
            generators.put(PacketType.Play.Client.CUSTOM_PAYLOAD, create(CPacketEventsCustomPayload.class));
            generators.put(PacketType.Play.Client.BLOCK_PLACE, create(CPacketEventsBlockPlace.class));
            generators.put(PacketType.Play.Client.WINDOW_CLICK, create(CPacketEventsWindowClick.class));
            generators.put(PacketType.Play.Client.SET_CREATIVE_SLOT, create(CPacketEventsSetCreativeSlot.class));
            generators.put(PacketType.Play.Client.ENTITY_ACTION, create(CPacketEventsEntityAction.class));
            generators.put(PacketType.Play.Client.ABILITIES, create(CPacketEventsAbilities.class));
            generators.put(PacketType.Play.Client.HELD_ITEM_SLOT, create(CPacketEventsHeldItemSlot.class));
            generators.put(PacketType.Play.Client.CHAT, create(CPacketEventsChat.class));
            generators.put(PacketType.Play.Client.TAB_COMPLETE, create(CPacketEventsTabComplete.class));
            generators.put(PacketType.Play.Client.SPECTATE, create(CPacketEventsSpectate.class));
            generators.put(PacketType.Play.Client.FLYING, create(CPacketEventsFlying.class));
            generators.put(PacketType.Play.Client.POSITION, create(CPacketEventsPosition.class));
            generators.put(PacketType.Play.Client.POSITION_LOOK, create(CPacketEventsPositionRotation.class));
            generators.put(PacketType.Play.Client.LOOK, create(CPacketEventsRotation.class));
        }
    }

    public final Translator<PacketPlayReceiveEvent, Packet> PACKET = new Translator<PacketPlayReceiveEvent, Packet>() {
        @Override
        public Packet transform(PacketPlayReceiveEvent from) {
            val player = from.getPlayer();
            val channel = (io.netty.channel.Channel) PacketEvents.get().getPlayerUtils().getChannel(player);
            val packetId = from.getPacketId();

            final PacketEventsTranslationHelper.PacketGenerator<?, ?> generator = generators.get(packetId);

            if (generator == null) {
                return null;
            }

            return generator.build(from.getNMSPacket(), MCPlayer.from(player));
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
