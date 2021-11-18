package io.fairyproject.mc;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public enum GameMode {
    CREATIVE(1),

    /**
     * Survival mode is the "normal" gameplay type, with no special features.
     */
    SURVIVAL(0),

    /**
     * Adventure mode cannot break blocks without the correct tools.
     */
    ADVENTURE(2),

    /**
     * Spectator mode cannot interact with the world in anyway and is
     * invisible to normal players. This grants the player the
     * ability to no-clip through the world.
     */
    SPECTATOR(3);

    private final int value;
    private final static Map<Integer, GameMode> BY_ID;

    private GameMode(final int value) {
        this.value = value;
    }

    /**
     * Gets the mode value associated with this GameMode
     *
     * @return An integer value of this gamemode
     */
    public int getValue() {
        return value;
    }

    /**
     * Gets the GameMode represented by the specified value
     *
     * @param value Value to check
     * @return Associative {@link GameMode} with the given value, or null if
     *     it doesn't exist
     */
    public static GameMode getByValue(final int value) {
        return BY_ID.get(value);
    }

    static {
        final ImmutableMap.Builder<Integer, GameMode> builder = ImmutableMap.builder();
        for (GameMode mode : values()) {
            builder.put(mode.getValue(), mode);
        }
        BY_ID = builder.build();
    }
}
