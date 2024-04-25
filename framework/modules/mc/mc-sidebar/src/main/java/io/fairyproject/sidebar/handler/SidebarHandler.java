package io.fairyproject.sidebar.handler;

import io.fairyproject.sidebar.Sidebar;
import io.fairyproject.sidebar.SidebarLine;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface SidebarHandler {

    void sendObjective(@NotNull Sidebar sidebar);

    void removeObjective(@NotNull Sidebar sidebar);

    void sendTitle(@NotNull Sidebar sidebar);

    void sendLine(@NotNull Sidebar sidebar, int index, @NotNull SidebarLine line);

    void removeLine(@NotNull Sidebar sidebar, int index);

}
