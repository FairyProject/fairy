package io.fairyproject.bukkit.protocol.packet.packetevents.v2.mapping;

import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.fairyproject.bukkit.protocol.packet.AbstractPacketMapping;
import io.fairyproject.bukkit.protocol.packet.packetevents.v2.wrappers.*;

public class PacketEventsV2Mapping extends AbstractPacketMapping<PacketWrapper<?>, PacketTypeCommon> {
    @Override
    public void inject() {
        create(PacketType.Play.Client.PLUGIN_MESSAGE, CPacketEventsCustomPayload.class, CPacketEventsCustomPayload::new);
        create(PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT, CPacketEventsBlockPlace.class, CPacketEventsBlockPlace::new);
        create(PacketType.Play.Client.CLICK_WINDOW, CPacketEventsWindowClick.class, CPacketEventsWindowClick::new);
        create(PacketType.Play.Client.CREATIVE_INVENTORY_ACTION, CPacketEventsSetCreativeSlot.class, CPacketEventsSetCreativeSlot::new);
        create(PacketType.Play.Client.ENTITY_ACTION, CPacketEventsEntityAction.class, CPacketEventsEntityAction::new);
        create(PacketType.Play.Client.PLAYER_ABILITIES, CPacketEventsAbilities.class, CPacketEventsAbilities::new);
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
