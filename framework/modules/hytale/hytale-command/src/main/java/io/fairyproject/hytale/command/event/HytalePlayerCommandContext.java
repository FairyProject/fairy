/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.fairyproject.hytale.command.event;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;

/**
 * Hytale player command context implementation.
 * This context is used for commands executed by players and provides access to
 * player-specific data like the player reference, world, and entity store.
 *
 * <p>Note: Hytale is multi-threaded, so command execution may occur on different threads.
 * This context captures the player's state at the time of command execution.</p>
 */
public class HytalePlayerCommandContext extends HytaleCommandContext {

    private final Store<EntityStore> store;
    private final Ref<EntityStore> ref;
    private final PlayerRef playerRef;
    private final World world;

    public HytalePlayerCommandContext(
            CommandSender sender,
            String[] args,
            Store<EntityStore> store,
            Ref<EntityStore> ref,
            PlayerRef playerRef,
            World world
    ) {
        super(sender, args);
        this.store = store;
        this.ref = ref;
        this.playerRef = playerRef;
        this.world = world;
    }

    @Override
    public String name() {
        return playerRef.getUsername();
    }

    /**
     * Get the entity store for the player.
     *
     * @return the entity store
     */
    public Store<EntityStore> getStore() {
        return store;
    }

    /**
     * Get the entity reference for the player.
     *
     * @return the entity reference
     */
    public Ref<EntityStore> getRef() {
        return ref;
    }

    /**
     * Get the player reference.
     *
     * @return the player reference
     */
    public PlayerRef getPlayerRef() {
        return playerRef;
    }

    /**
     * Get the world the player is in.
     *
     * @return the world
     */
    public World getWorld() {
        return world;
    }

    /**
     * Get the player's UUID.
     *
     * @return the player's UUID
     */
    public UUID getPlayerUuid() {
        return playerRef.getUuid();
    }

    /**
     * Get the player's username.
     *
     * @return the player's username
     */
    public String getPlayerName() {
        return playerRef.getUsername();
    }

    /**
     * Check if the player reference is still valid.
     * In a multi-threaded environment, the player may have disconnected.
     *
     * @return true if the player reference is still valid
     */
    public boolean isPlayerValid() {
        return playerRef.isValid() && ref.isValid();
    }

    /**
     * Check if we are currently on the correct thread for this store.
     *
     * @return true if on the correct thread
     */
    public boolean isInStoreThread() {
        return store.isInThread();
    }
}
