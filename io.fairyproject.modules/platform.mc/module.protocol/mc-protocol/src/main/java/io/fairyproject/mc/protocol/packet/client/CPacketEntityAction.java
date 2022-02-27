package io.fairyproject.mc.protocol.packet.client;

import io.fairyproject.mc.mcp.PlayerAction;

public interface CPacketEntityAction extends CPacket {
    /**
     * Returns the player action which was executed; The following actions are valid depending on
     * the player's protocol version:
     * 0 -> START_SNEAKING
     * 1 -> STOP_SNEAKING
     * 2 -> STOP_SLEEPING
     * 3 -> START_SPRINTING
     * 4 -> STOP_SPRINTING
     * 5 -> START_RIDING_JUMP
     * 6 -> STOP_RIDING_JUMP
     * 7 -> OPEN_INVENTORY
     * 8 -> START_FALL_FLYING
     * @return Returns the player action
     */
    PlayerAction getAction();

    @Override
    default String getFancyName() {
        return "EntityAction";
    }
}
