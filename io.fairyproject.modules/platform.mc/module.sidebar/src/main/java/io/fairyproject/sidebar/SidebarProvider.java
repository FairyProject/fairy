package io.fairyproject.sidebar;

import io.fairyproject.mc.MCPlayer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a sidebar provider.
 */
public interface SidebarProvider {

    /**
     * Gets the title of the sidebar.
     *
     * @param mcPlayer the player to display the sidebar
     * @return the title of the sidebar, null will not display the sidebar
     */
    @Nullable
    Component getTitle(@NotNull MCPlayer mcPlayer);

    /**
     * Gets the lines of the sidebar.
     *
     * @param mcPlayer the player to display the sidebar
     * @return the lines of the sidebar, null will not display the sidebar
     */
    @Nullable
    List<SidebarLine> getLines(@NotNull MCPlayer mcPlayer);

    /**
     * Gets the priority of the sidebar provider.
     *
     * @return the priority of the sidebar provider, higher priority will be displayed first
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Checks if the sidebar should be displayed.
     *
     * @param mcPlayer the player to display the sidebar
     * @return true if the sidebar should be displayed, false otherwise
     */
    default boolean shouldDisplay(@NotNull MCPlayer mcPlayer) {
        return true;
    }

    /**
     * Called when the sidebar is shown to the player.
     *
     * @param mcPlayer the player to display the sidebar
     * @param sidebar the sidebar to be displayed
     */
    default void onSidebarShown(@NotNull MCPlayer mcPlayer, @NotNull Sidebar sidebar) {
        // to be overridden
    }

    /**
     * Called when the sidebar is hidden from the player.
     *
     * @param mcPlayer the player to display the sidebar
     * @param sidebar the sidebar to be hidden
     */
    default void onSidebarHidden(@NotNull MCPlayer mcPlayer, @NotNull Sidebar sidebar) {
        // to be overridden
    }

}
