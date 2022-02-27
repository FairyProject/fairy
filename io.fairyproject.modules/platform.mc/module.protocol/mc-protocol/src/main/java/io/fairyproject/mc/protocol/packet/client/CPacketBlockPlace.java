package io.fairyproject.mc.protocol.packet.client;

import io.fairyproject.mc.mcp.Direction;
import io.fairyproject.mc.mcp.Hand;
import io.fairyproject.mc.util.Vec3f;
import io.fairyproject.mc.util.Vec3i;

import java.util.Optional;

public interface CPacketBlockPlace extends CPacket {

    Hand getHand();

    Direction getDirection();

    Vec3i getClickedBlock();

    /**
     *
     * @return
     */
    Optional<Vec3f> getClickedOffset();

    /**
     * Returns the ItemStack type used for placing a block. This can vary depending the
     * scenario (such as direction 255) as to where the following won't be provided.
     *
     * @param <T> Type parameter representing the ItemStack type defined by the wrapper
     * @return ItemStack used for the block place. This can be empty for hand clicks.
     */
    <T> Optional<T> getItemStack();

    @Override
    default String getFancyName() {
        return "BlockPlace";
    }
}
