package io.fairyproject.bukkit.protocol.packet.packetevents.v1.mapping;

import io.fairyproject.bukkit.protocol.packet.AbstractPacketMapping;
import io.fairyproject.bukkit.protocol.packet.packetevents.v1.wrappers.*;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.packet.Packet;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;

public class PacketEventsV1Mapping extends AbstractPacketMapping<WrappedPacket, Byte> {
    @Override
    public void inject() {
        create(PacketType.Play.Client.CUSTOM_PAYLOAD, CPacketEventsCustomPayload.class, CPacketEventsCustomPayload::new);
        create(PacketType.Play.Client.BLOCK_PLACE, CPacketEventsBlockPlace.class, CPacketEventsBlockPlace::new);
        create(PacketType.Play.Client.WINDOW_CLICK, CPacketEventsWindowClick.class, CPacketEventsWindowClick::new);
        create(PacketType.Play.Client.SET_CREATIVE_SLOT, CPacketEventsSetCreativeSlot.class, CPacketEventsSetCreativeSlot::new);
        create(PacketType.Play.Client.ENTITY_ACTION, CPacketEventsEntityAction.class, CPacketEventsEntityAction::new);
        create(PacketType.Play.Client.ABILITIES, CPacketEventsAbilities.class, CPacketEventsAbilities::new);
        create(PacketType.Play.Client.HELD_ITEM_SLOT, CPacketEventsHeldItemSlot.class, CPacketEventsHeldItemSlot::new);
        create(PacketType.Play.Client.CHAT, CPacketEventsChat.class, CPacketEventsChat::new);
        create(PacketType.Play.Client.TAB_COMPLETE, CPacketEventsTabComplete.class, CPacketEventsTabComplete::new);
        create(PacketType.Play.Client.SPECTATE, CPacketEventsSpectate.class, CPacketEventsSpectate::new);
        create(PacketType.Play.Client.FLYING, CPacketEventsFlying.class, CPacketEventsFlying::new);
        create(PacketType.Play.Client.POSITION, CPacketEventsPosition.class, CPacketEventsPosition::new);
        create(PacketType.Play.Client.POSITION_LOOK, CPacketEventsPositionRotation.class, CPacketEventsPositionRotation::new);
        create(PacketType.Play.Client.LOOK, CPacketEventsRotation.class, CPacketEventsRotation::new);
    }
}
