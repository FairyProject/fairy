package io.fairyproject.mc.hologram;

import io.fairyproject.mc.MCEntity;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.Viewable;
import io.fairyproject.mc.hologram.line.HologramLine;
import io.fairyproject.mc.util.Position;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public interface Hologram extends Viewable {

    static @NotNull Hologram create(Position pos) {
        return new HologramImpl(pos);
    }

    /**
     * Allow the hologram to automatically be displayed to players.
     *
     * @return this
     */
    @Contract("_ -> this")
    Hologram autoViewable(boolean autoViewable);

    /**
     * Set the view distance that this hologram can be viewed.
     * This is only useful if it's auto viewable.
     *
     * @param viewDistance the view distance
     * @return this
     */
    @Contract("_ -> this")
    Hologram viewDistance(int viewDistance);

    /**
     * Set the handler whenever player attacks the hologram entity.
     *
     * @param attackHandler the handler
     * @return this
     */
    @Contract("_ -> this")
    Hologram attackHandler(Consumer<MCPlayer> attackHandler);

    /**
     * Set the handler whenever player interacts the hologram entity.
     *
     * @param interactHandler the handler
     * @return this
     */
    @Contract("_ -> this")
    Hologram interactHandler(Consumer<MCPlayer> interactHandler);

    /**
     * Replace the lines to display for the hologram.
     *
     * @return this
     */
    @Contract("_ -> this")
    Hologram lines(@NotNull List<HologramLine> lines);

    /**
     * Append a line to display for the hologram.
     *
     * @return this
     */
    @Contract("_ -> this")
    Hologram line(@NotNull HologramLine line);

    /**
     * Append a line at certain index to display for the hologram.
     *
     * @return this
     */
    @Contract("_, _ -> this")
    Hologram line(int index, @NotNull HologramLine line);

    /**
     * Set the position of the hologram.
     *
     * @return this
     */
    @Contract("_ -> this")
    Hologram position(@NotNull Position pos);

    /**
     * Set the vertical spacing between each line.
     *
     * @param verticalSpacing the spacing
     * @return this
     */
    Hologram verticalSpacing(double verticalSpacing);

    /**
     * Attach the hologram to an entity.
     *
     * @return this
     */
    @Contract("_ -> this")
    Hologram attach(@Nullable MCEntity entity);

    /**
     * Remove a line from a certain index.
     */
    void removeLine(int index);

    /**
     * Clear every line of the hologram.
     */
    void clear();

    /**
     * Spawn the hologram.
     *
     * @return this
     */
    @Contract(" -> this")
    Hologram spawn();

    /**
     * Remove the hologram.
     */
    void remove();

    /**
     * Check if the hologram were spawned.
     *
     * @return true if spawned
     */
    boolean isSpawned();

    /**
     * Check if this hologram are automatically be displayed to players.
     *
     * @return true if auto viewable
     */
    boolean isAutoViewable();

    /**
     * Get the view distance of the hologram.
     *
     * @return the view distance
     */
    int getViewDistance();

    /**
     * Get the entity that are attached by this hologram.
     *
     * @return entity
     */
    @Nullable MCEntity getAttached();

    /**
     * Get the current position of the hologram.
     *
     * @return the hologram
     */
    @NotNull Position getPosition();

    /**
     * Get the vertical spacing between each line of the hologram.
     *
     * @return the vertical spacing
     */
    double getVerticalSpacing();

    /**
     * Get the lines of the hologram.
     *
     * @return the lines
     */
    @NotNull List<HologramLine> getLines();

}
