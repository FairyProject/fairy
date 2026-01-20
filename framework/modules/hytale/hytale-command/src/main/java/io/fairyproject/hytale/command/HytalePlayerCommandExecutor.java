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
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.fairyproject.command.BaseCommand;
import io.fairyproject.hytale.command.event.HytalePlayerCommandContext;
import lombok.Getter;

/**
 * Hytale player command executor that bridges Fairy's BaseCommand to Hytale's command system.
 *
 * <p>This executor extends {@link AbstractPlayerCommand} which handles multi-threading properly.
 * The execute method is called on the correct thread for the player's world, ensuring thread safety
 * when accessing player and world data.</p>
 *
 * <p>The command execution flow:</p>
 * <ol>
 *   <li>Player executes command (may be on any thread)</li>
 *   <li>AbstractPlayerCommand.executeAsync is called</li>
 *   <li>Hytale schedules execution on the correct world thread</li>
 *   <li>execute() is called with player context on the correct thread</li>
 *   <li>Fairy's BaseCommand.execute() is invoked with HytalePlayerCommandContext</li>
 * </ol>
 */
@Getter
public class HytalePlayerCommandExecutor extends AbstractPlayerCommand {

    private final BaseCommand fairyCommand;

    public HytalePlayerCommandExecutor(BaseCommand fairyCommand) {
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
    protected void execute(
            CommandContext hytaleContext,
            Store<EntityStore> store,
            Ref<EntityStore> ref,
            PlayerRef playerRef,
            World world
    ) {
        // Extract the remaining arguments from the input
        String inputString = hytaleContext.getInputString();
        String[] args = extractArgs(inputString);

        // Create player-specific context with all the player/world information
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
