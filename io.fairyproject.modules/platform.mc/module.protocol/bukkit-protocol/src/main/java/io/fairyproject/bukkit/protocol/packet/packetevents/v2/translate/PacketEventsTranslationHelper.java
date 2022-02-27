package io.fairyproject.bukkit.protocol.packet.packetevents.v2.translate;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketEvent;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import io.fairyproject.bukkit.protocol.packet.packetevents.v2.PacketEventWrapper;
import io.fairyproject.bukkit.protocol.packet.packetevents.v2.netty.PacketEventsChannel;
import io.fairyproject.bukkit.protocol.packet.packetevents.v2.wrappers.*;
import io.fairyproject.bukkit.reflection.wrapper.ConstructorWrapper;
import io.fairyproject.mc.mcp.Direction;
import io.fairyproject.mc.mcp.Hand;
import io.fairyproject.mc.mcp.ObjectiveActionType;
import io.fairyproject.mc.mcp.PlayerAction;
import io.fairyproject.mc.protocol.item.ObjectiveRenderType;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.packet.Packet;
import io.fairyproject.mc.protocol.packet.client.CPacket;
import io.fairyproject.mc.protocol.translate.Translator;
import io.fairyproject.mc.util.Vec3f;
import io.fairyproject.mc.util.Vec3i;
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

    private final Map<PacketTypeCommon, PacketGenerator<?, ?>> generators = new HashMap<>();

    static class PacketGenerator<T, W extends PacketEventWrapper<T>> {
        private final ConstructorWrapper<T> typeConstructor;
        private final ConstructorWrapper<W> wrapperConstructor;

        public PacketGenerator(Class<T> typeClass, Class<W> wrapperClass, boolean client) {
            Constructor<T> _typeConstructor;
            try {
                _typeConstructor = typeClass.getDeclaredConstructor(client ? PacketReceiveEvent.class : PacketSendEvent.class);
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

        public W build(final PacketEvent event, final MCPlayer channel) {
            final T instance = typeConstructor.newInstance(event);
            return wrapperConstructor.newInstance(instance, channel);
        }

        static <T, W extends PacketEventWrapper<T>> PacketGenerator<T, W> create(Class<W> wrapperClass) {
            final Class<T> typeClass = (Class<T>) (ParameterizedType.class.cast(wrapperClass.getGenericSuperclass())).getActualTypeArguments()[0];
            return new PacketGenerator<>(typeClass, wrapperClass, CPacket.class.isAssignableFrom(wrapperClass));
        }

        static  {
            generators.put(PacketType.Play.Client.PLUGIN_MESSAGE, create(CPacketEventsCustomPayload.class));
            generators.put(PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT, create(CPacketEventsBlockPlace.class));
            generators.put(PacketType.Play.Client.CLICK_WINDOW, create(CPacketEventsWindowClick.class));
            generators.put(PacketType.Play.Client.CREATIVE_INVENTORY_ACTION, create(CPacketEventsSetCreativeSlot.class));
            generators.put(PacketType.Play.Client.ENTITY_ACTION, create(CPacketEventsEntityAction.class));
            generators.put(PacketType.Play.Client.PLAYER_ABILITIES, create(CPacketEventsAbilities.class));
            generators.put(PacketType.Play.Client.HELD_ITEM_CHANGE, create(CPacketEventsHeldItemSlot.class));
            generators.put(PacketType.Play.Client.CHAT_MESSAGE, create(CPacketEventsChat.class));
            generators.put(PacketType.Play.Client.TAB_COMPLETE, create(CPacketEventsTabComplete.class));
            generators.put(PacketType.Play.Client.SPECTATE, create(CPacketEventsSpectate.class));
            generators.put(PacketType.Play.Client.PLAYER_FLYING, create(CPacketEventsFlying.class));
            generators.put(PacketType.Play.Client.PLAYER_POSITION, create(CPacketEventsPosition.class));
            generators.put(PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION, create(CPacketEventsPositionRotation.class));
            generators.put(PacketType.Play.Client.PLAYER_ROTATION, create(CPacketEventsRotation.class));

            generators.put(PacketType.Play.Server.SCOREBOARD_OBJECTIVE, create(SPacketEventsScoreboardObjective.class));
        }
    }

    public final Translator<PacketPlayReceiveEvent, Packet> PACKET = new Translator<PacketPlayReceiveEvent, Packet>() {
        @Override
        public Packet transform(PacketPlayReceiveEvent from) {
            val player = from.getPlayer();
            val packetId = from.getPacketType();

            final PacketGenerator<?, ?> generator = generators.get(packetId);

            if (generator == null) {
                return null;
            }

            return generator.build(from, MCPlayer.from(player));
        }
    };
    
    public final Translator<InteractionHand, Hand> HAND = new Translator<InteractionHand, Hand>() {
        @Override
        public Hand transform(InteractionHand from) {
            return Hand.values()[from.ordinal()];
        }
    };

    public final Translator<BlockFace, Direction> DIRECTION = new Translator<BlockFace, Direction>() {
        @Override
        public Direction transform(BlockFace from) {
            return Direction.getDirection(from.getFaceValue());
        }
    };

    public final Translator<WrapperPlayClientEntityAction.Action, PlayerAction> PLAYER_ACTION = new Translator<WrapperPlayClientEntityAction.Action, PlayerAction>() {
        @Override
        public PlayerAction transform(WrapperPlayClientEntityAction.Action from) {
            return PlayerAction.values()[from.ordinal()];
        }
    };

    public final Translator<WrapperPlayServerScoreboardObjective.HealthDisplay, ObjectiveRenderType> SCOREBOARD_DISPLAY_TYPE = new Translator<WrapperPlayServerScoreboardObjective.HealthDisplay, ObjectiveRenderType>() {
        @Override
        public ObjectiveRenderType transform(WrapperPlayServerScoreboardObjective.HealthDisplay from) {
            return from == WrapperPlayServerScoreboardObjective.HealthDisplay.HEARTS ? ObjectiveRenderType.HEARTS : ObjectiveRenderType.INTEGER;
        }
    };

    public final Translator<WrapperPlayServerScoreboardObjective.ObjectiveMode, ObjectiveActionType> SCOREBOARD_ACTION_TYPE = new Translator<WrapperPlayServerScoreboardObjective.ObjectiveMode, ObjectiveActionType>() {
        @Override
        public ObjectiveActionType transform(WrapperPlayServerScoreboardObjective.ObjectiveMode from) {
            return ObjectiveActionType.values()[from.ordinal()];
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
