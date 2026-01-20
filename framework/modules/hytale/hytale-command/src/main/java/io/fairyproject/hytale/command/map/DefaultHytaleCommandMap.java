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

package io.fairyproject.hytale.command.map;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandRegistration;
import com.hypixel.hytale.server.core.command.system.CommandRegistry;
import io.fairyproject.command.BaseCommand;
import io.fairyproject.command.annotation.Command;
import io.fairyproject.container.InjectableComponent;
import io.fairyproject.data.MetaKey;
import io.fairyproject.data.MetaStorage;
import io.fairyproject.hytale.FairyHytalePlatform;
import io.fairyproject.hytale.command.HytaleCommandExecutor;
import io.fairyproject.hytale.command.HytalePlayerCommandExecutor;
import io.fairyproject.hytale.command.event.HytalePlayerCommandContext;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Default implementation of HytaleCommandMap that registers commands with Hytale's CommandRegistry.
 *
 * <p>This implementation supports two types of command executors:</p>
 * <ul>
 *   <li>{@link HytaleCommandExecutor} - For general commands that can be executed by any sender</li>
 *   <li>{@link HytalePlayerCommandExecutor} - For player-only commands that need thread-safe access to player/world data</li>
 * </ul>
 *
 * <p>The executor type is automatically detected by checking if any command method uses
 * {@link HytalePlayerCommandContext} as a parameter.</p>
 */
@InjectableComponent
public class DefaultHytaleCommandMap implements HytaleCommandMap {

    public static final MetaKey<AbstractCommand> EXECUTOR_KEY = MetaKey.create("fairy:hytale-command-executor", AbstractCommand.class);
    public static final MetaKey<CommandRegistration> REGISTRATION_KEY = MetaKey.create("fairy:hytale-command-registration", CommandRegistration.class);

    public DefaultHytaleCommandMap() {
        System.out.println("DefaultHytaleCommandMap initialized");
    }

    @Override
    public void register(BaseCommand command) {
        if (this.isRegistered(command)) {
            throw new IllegalArgumentException("Command already registered: " + command.getCommandNames()[0]);
        }

        // Auto-detect if this is a player command by checking method parameters
        boolean isPlayerCommand = hasPlayerCommandContext(command.getClass());

        AbstractCommand commandExecutor;
        if (isPlayerCommand) {
            commandExecutor = new HytalePlayerCommandExecutor(command);
        } else {
            commandExecutor = new HytaleCommandExecutor(command);
        }

        CommandRegistry commandRegistry = getCommandRegistry();
        CommandRegistration registration = commandRegistry.registerCommand(commandExecutor);

        command.getMetaStorage().put(EXECUTOR_KEY, commandExecutor);
        command.getMetaStorage().put(REGISTRATION_KEY, registration);
    }

    /**
     * Check if any @Command annotated method in the class uses HytalePlayerCommandContext as a parameter.
     */
    private boolean hasPlayerCommandContext(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Command.class)) {
                continue;
            }

            for (Parameter parameter : method.getParameters()) {
                if (HytalePlayerCommandContext.class.isAssignableFrom(parameter.getType())) {
                    return true;
                }
            }
        }

        // Also check superclass methods
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && superclass != Object.class) {
            return hasPlayerCommandContext(superclass);
        }

        return false;
    }

    @Override
    public void unregister(BaseCommand command) {
        MetaStorage metaStorage = command.getMetaStorage();
        if (!this.isRegistered(command)) {
            throw new IllegalArgumentException("Command not registered: " + command.getCommandNames()[0]);
        }

        // Note: Hytale's CommandRegistry may not support unregistration
        // Remove from meta storage to mark as unregistered
        metaStorage.remove(EXECUTOR_KEY);
        metaStorage.remove(REGISTRATION_KEY);
    }

    @Override
    public boolean isRegistered(BaseCommand command) {
        return command.getMetaStorage().contains(EXECUTOR_KEY);
    }

    private CommandRegistry getCommandRegistry() {
        return FairyHytalePlatform.PLUGIN.getCommandRegistry();
    }
}
