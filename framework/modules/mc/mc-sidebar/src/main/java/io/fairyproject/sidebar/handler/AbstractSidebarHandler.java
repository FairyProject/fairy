package io.fairyproject.sidebar.handler;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisplayScoreboard;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective.ObjectiveMode;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective.RenderType;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.sidebar.Sidebar;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractSidebarHandler implements SidebarHandler {
    @Override
    public void sendObjective(@NotNull Sidebar sidebar) {
        MCPlayer player = sidebar.getPlayer();
        WrapperPlayServerScoreboardObjective objective = new WrapperPlayServerScoreboardObjective(player.getName(), ObjectiveMode.CREATE, sidebar.getTitle(), RenderType.INTEGER);
        WrapperPlayServerDisplayScoreboard scoreboard = new WrapperPlayServerDisplayScoreboard(1, player.getName());

        MCProtocol.sendPacket(player, objective);
        MCProtocol.sendPacket(player, scoreboard);
    }

    @Override
    public void removeObjective(@NotNull Sidebar sidebar) {
        MCPlayer player = sidebar.getPlayer();
        WrapperPlayServerScoreboardObjective packet = new WrapperPlayServerScoreboardObjective(player.getName(), ObjectiveMode.REMOVE, Component.empty(), RenderType.INTEGER);

        MCProtocol.sendPacket(player, packet);
    }

    @Override
    public void sendTitle(@NotNull Sidebar sidebar) {
        MCPlayer player = sidebar.getPlayer();
        WrapperPlayServerScoreboardObjective packet = new WrapperPlayServerScoreboardObjective(player.getName(), ObjectiveMode.UPDATE, sidebar.getTitle(), RenderType.INTEGER);

        MCProtocol.sendPacket(player, packet);
    }
}
