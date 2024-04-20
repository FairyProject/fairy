package io.fairyproject.sidebar.handler.modern;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerResetScore;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.sidebar.Sidebar;
import io.fairyproject.sidebar.SidebarLine;
import io.fairyproject.sidebar.handler.AbstractSidebarHandler;
import org.jetbrains.annotations.NotNull;

/**
 * We can't use this for now because Lunar Client doesn't support custom display name directly from update score packet as of April/19/2024.
 */
public class ModernSidebarHandler extends AbstractSidebarHandler {
    @Override
    public void sendLine(@NotNull Sidebar sidebar, int index, @NotNull SidebarLine line) {
        MCPlayer player = sidebar.getPlayer();
        WrapperPlayServerUpdateScore packet = new WrapperPlayServerUpdateScore(
                "-sb" + index,
                WrapperPlayServerUpdateScore.Action.CREATE_OR_UPDATE_ITEM,
                player.getName(),
                index,
                line.getComponent(),
                line.getFormat()
        );

        MCProtocol.sendPacket(player, packet);
    }

    @Override
    public void removeLine(@NotNull Sidebar sidebar, int index) {
        MCPlayer player = sidebar.getPlayer();
        WrapperPlayServerResetScore packet = new WrapperPlayServerResetScore("-sb" + index, player.getName());

        MCProtocol.sendPacket(player, packet);
    }
}
