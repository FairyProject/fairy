package io.fairyproject.mc;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface Viewable {

    /**
     * Adds a viewer.
     *
     * @param player the viewer to add
     * @return true if the player has been added, false otherwise (could be because he is already a viewer)
     */
    boolean addViewer(@NotNull MCPlayer player);

    /**
     * Removes a viewer.
     *
     * @param player the viewer to remove
     * @return true if the player has been removed, false otherwise (could be because he was not a viewer)
     */
    boolean removeViewer(@NotNull MCPlayer player);

    /**
     * Gets all the viewers of this viewable element.
     *
     * @return A Set containing all the element's viewers
     */
    @NotNull Set<@NotNull MCPlayer> getViewers();

    /**
     * Gets if a player is seeing this viewable object.
     *
     * @param player the player to check
     * @return true if {@code player} is a viewer, false otherwise
     */
    default boolean isViewer(@NotNull MCPlayer player) {
        return getViewers().contains(player);
    }

}
