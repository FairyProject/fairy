package io.fairyproject.sidebar.handler.modern;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerResetScore;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.sidebar.Sidebar;
import io.fairyproject.sidebar.handler.legacy.V13LegacySidebarHandler;

/**
 * This class is used to fix Lunar Client compatibility issue (using team packet instead of display name from score packet).
 */
public class LunarFixModernSidebarHandler extends V13LegacySidebarHandler {

    @Override
    protected PacketWrapper<?> getResetScorePacket(Sidebar sidebar, int index) {
        MCPlayer player = sidebar.getPlayer();

        return new WrapperPlayServerResetScore(getEntry(index), player.getName());
    }

}
