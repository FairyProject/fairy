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

package io.fairyproject.hytale.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.fairyproject.command.BaseCommand;
import io.fairyproject.hytale.command.event.HytaleCommandContext;
import io.fairyproject.hytale.command.event.HytalePlayerCommandContext;
import lombok.Getter;

import java.util.concurrent.CompletableFuture;

/**
 * Hytale mixed command executor that supports both player and console senders.
 *
 * <p>This executor extends {@link AbstractAsyncCommand} which accepts any sender type, allowing
 * both players and console to execute commands. When the sender is a player, it schedules
 * execution on the correct world thread and creates a {@link HytalePlayerCommandContext} with
 * full player data. When the sender is not a player (e.g., console), it creates a
 * {@link HytaleCommandContext} and executes immediately.</p>
 *
 * <p>This allows commands to have some sub-commands that require player context and others
 * that work with any sender. The Fairy command framework handles context type validation
 * at the method level:</p>
 * <ul>
 *   <li>Methods using {@link HytaleCommandContext} can be executed by any sender (player or console)</li>
 *   <li>Methods using {@link HytalePlayerCommandContext} can only be executed by players</li>
 * </ul>
 *
 * @see HytaleCommandExecutor for commands where all methods use HytaleCommandContext
 * @see HytalePlayerCommandExecutor for commands where all methods use HytalePlayerCommandContext
 */
@Getter
public class HytaleMixedCommandExecutor extends AbstractAsyncCommand {

    private static final Message MESSAGE_PLAYER_NOT_IN_WORLD = Message.raw("You must be in a world to execute this command.").color("#FF5555");

    private final BaseCommand fairyCommand;

    public HytaleMixedCommandExecutor(BaseCommand fairyCommand) {
        super(fairyCommand.getCommandNames()[0], fairyCommand.getDescription());
        this.fairyCommand = fairyCommand;

        // Add aliases
        String[] commandNames = fairyCommand.getCommandNames();
        if (commandNames.length > 1) {
            String[] aliases = new String[commandNames.length - 1];
            System.arraycopy(commandNames, 1, aliases, 0, aliases.length);
            this.addAliases(aliases);
        }

        // Allow extra arguments since Fairy handles its own argument parsing
        this.setAllowsExtraArguments(true);
    }

    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext hytaleContext) {
        // Check if sender is a player
        if (hytaleContext.isPlayer()) {
            // Get player reference from context
            Ref<EntityStore> ref = hytaleContext.senderAsPlayerRef();

            if (ref == null || !ref.isValid()) {
                hytaleContext.sendMessage(MESSAGE_PLAYER_NOT_IN_WORLD);
                return CompletableFuture.completedFuture(null);
            }

            // Get Store and World (these don't require thread safety)
            Store<EntityStore> store = ref.getStore();
            EntityStore entityStore = (EntityStore) store.getExternalData();
            World world = entityStore.getWorld();

            // Schedule execution on the correct world thread
            return runAsync(hytaleContext, () -> executeForPlayer(hytaleContext, store, ref, world), world);
        } else {
            // Non-player sender (console, etc.) - execute immediately
            executeForConsole(hytaleContext);
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * Execute command for player on the correct world thread.
     */
    private void executeForPlayer(CommandContext hytaleContext, Store<EntityStore> store, Ref<EntityStore> ref, World world) {
        String[] args = extractArgs(hytaleContext.getInputString());

        // Now we're on the correct thread, safe to call store.getComponent()
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

        HytalePlayerCommandContext fairyContext = new HytalePlayerCommandContext(
                hytaleContext.sender(),
                args,
                store,
                ref,
                playerRef,
                world
        );

        try {
            this.fairyCommand.execute(fairyContext);
        } catch (Throwable throwable) {
            this.fairyCommand.onError(fairyContext, throwable);
            hytaleContext.sendMessage(Message.raw("An error occurred while executing the command.").color("#FF5555"));
        }
    }

    /**
     * Execute command for console/non-player sender.
     */
    private void executeForConsole(CommandContext hytaleContext) {
        String[] args = extractArgs(hytaleContext.getInputString());

        HytaleCommandContext fairyContext = new HytaleCommandContext(hytaleContext.sender(), args);

        try {
            this.fairyCommand.execute(fairyContext);
        } catch (Throwable throwable) {
            this.fairyCommand.onError(fairyContext, throwable);
            hytaleContext.sendMessage(Message.raw("An error occurred while executing the command.").color("#FF5555"));
        }
    }

    /**
     * Extract arguments from the input string.
     * The input string contains the full command, so we need to skip the command name.
     */
    private String[] extractArgs(String inputString) {
        if (inputString == null || inputString.isEmpty()) {
            return new String[0];
        }

        String[] parts = inputString.split(" ");
        if (parts.length <= 1) {
            return new String[0];
        }

        // Skip the command name and return the rest as arguments
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);
        return args;
    }
}
