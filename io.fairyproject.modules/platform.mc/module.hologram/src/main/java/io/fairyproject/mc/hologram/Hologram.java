package io.fairyproject.mc.hologram;

import io.fairyproject.mc.MCEntity;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.Viewable;
import io.fairyproject.mc.hologram.line.HologramLine;
import io.fairyproject.mc.util.Pos;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public interface Hologram extends Viewable {

    static @NotNull Hologram create(Pos pos) {
        return new HologramImpl(pos);
    }

    /**
     * Allow the hologram to automatically be displayed to players.
     *
     * @return this
     */
    @Contract("_ -> this")
    Hologram withAutoViewable(boolean autoViewable);

    /**
     * Set the view distance that this hologram can be viewed.
     * This is only useful if it's auto viewable.
     *
     * @param viewDistance the view distance
     * @return this
     */
    @Contract("_ -> this")
    Hologram withViewDistance(int viewDistance);

    /**
     * Set the handler whenever player attacks the hologram entity.
     *
     * @param attackHandler the handler
     * @return this
     */
    @Contract("_ -> this")
    Hologram withAttackHandler(Consumer<MCPlayer> attackHandler);

    /**
     * Set the handler whenever player interacts the hologram entity.
     *
     * @param interactHandler the handler
     * @return this
     */
    @Contract("_ -> this")
    Hologram withInteractHandler(Consumer<MCPlayer> interactHandler);

    /**
     * Replace the lines to display for the hologram.
     *
     * @return this
     */
    @Contract("_ -> this")
    Hologram withLines(@NotNull List<HologramLine> lines);

    /**
     * Append a line to display for the hologram.
     *
     * @return this
     */
    @Contract("_ -> this")
    Hologram withLine(@NotNull HologramLine line);

    /**
     * Append a line at certain index to display for the hologram.
     *
     * @return this
     */
    @Contract("_, _ -> this")
    Hologram withLine(int index, @NotNull HologramLine line);

    /**
     * Set the position of the hologram.
     *
     * @return this
     */
    @Contract("_ -> this")
    Hologram withPos(@NotNull Pos pos);

    /**
     * Set the vertical spacing between each line.
     *
     * @param verticalSpacing the spacing
     * @return this
     */
    Hologram withVerticalSpacing(double verticalSpacing);

    /**
     * Attach the hologram to an entity.
     *
     * @return this
     */
    @Contract("_ -> this")
    Hologram withAttached(@Nullable MCEntity entity);

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
    int viewDistance();

    /**
     * Get the entity that are attached by this hologram.
     *
     * @return entity
     */
    @Nullable MCEntity attached();

    /**
     * Get the current position of the hologram.
     *
     * @return the hologram
     */
    @NotNull Pos pos();

    /**
     * Get the vertical spacing between each line of the hologram.
     *
     * @return the vertical spacing
     */
    double verticalSpacing();

    /**
     * Get the lines of the hologram.
     *
     * @return the lines
     */
    @NotNull List<HologramLine> lines();

}
