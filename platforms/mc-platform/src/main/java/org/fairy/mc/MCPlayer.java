package org.fairy.mc;

import java.util.UUID;

/**
 * A proxy player class for cross platform purposes
 */
public interface MCPlayer {

    /**
     * get player's UUID
     *
     * @return UUID
     */
    UUID getUUID();

    /**
     * get player's Name
     *
     * @return name
     */
    String getName();

    /**
     * cast the proxy player to platform specific player instance
     *
     * @param playerClass the platform specific Player class
     * @param <T> the type of the platform specific Player
     * @return the instance
     * @throws ClassCastException if class is incorrect, could be wrong type or not the right platform
     */
    <T> T as(Class<T> playerClass);

}
