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
import io.fairyproject.hytale.command.HytaleMixedCommandExecutor;
import io.fairyproject.hytale.command.HytalePlayerCommandExecutor;
import io.fairyproject.hytale.command.event.HytaleCommandContext;
import io.fairyproject.hytale.command.event.HytalePlayerCommandContext;
import io.fairyproject.log.Log;

import java.lang.reflect.Method;

/**
 * Default implementation of HytaleCommandMap that registers commands with Hytale's CommandRegistry.
 *
 * <p>This implementation supports three types of command executors:</p>
 * <ul>
 *   <li>{@link HytaleCommandExecutor} - For commands where all methods use HytaleCommandContext (any sender)</li>
 *   <li>{@link HytalePlayerCommandExecutor} - For commands where all methods use HytalePlayerCommandContext (player-only, thread-safe)</li>
 *   <li>{@link HytaleMixedCommandExecutor} - For commands with mixed context types (supports both player and console)</li>
 * </ul>
 *
 * <p>The executor type is automatically detected by analyzing the command method parameters:</p>
 * <ul>
 *   <li>If all methods use HytalePlayerCommandContext → HytalePlayerCommandExecutor</li>
 *   <li>If all methods use HytaleCommandContext → HytaleCommandExecutor</li>
 *   <li>If methods use both types → HytaleMixedCommandExecutor</li>
 * </ul>
 */
@InjectableComponent
public class DefaultHytaleCommandMap implements HytaleCommandMap {

    public static final MetaKey<AbstractCommand> EXECUTOR_KEY = MetaKey.create("fairy:hytale-command-executor", AbstractCommand.class);
    public static final MetaKey<CommandRegistration> REGISTRATION_KEY = MetaKey.create("fairy:hytale-command-registration", CommandRegistration.class);

    @Override
    public void register(BaseCommand command) {
        if (this.isRegistered(command)) {
            throw new IllegalArgumentException("Command already registered: " + command.getCommandNames()[0]);
        }

        Log.info("Registering command: " + command.getClass().getName());

        // Analyze command methods to determine executor type
        ContextTypeInfo contextInfo = analyzeContextTypes(command.getClass());

        AbstractCommand commandExecutor;
        if (contextInfo.hasOnlyPlayerContext()) {
            // All methods require player context - use player-only executor (thread-safe)
            commandExecutor = new HytalePlayerCommandExecutor(command);
        } else if (contextInfo.hasOnlyGeneralContext()) {
            // All methods use general context - use general executor (any sender)
            commandExecutor = new HytaleCommandExecutor(command);
        } else {
            // Mixed context types - use mixed executor (supports both player and console)
            commandExecutor = new HytaleMixedCommandExecutor(command);
        }

        CommandRegistry commandRegistry = getCommandRegistry();
        CommandRegistration registration = commandRegistry.registerCommand(commandExecutor);

        command.getMetaStorage().put(EXECUTOR_KEY, commandExecutor);
        command.getMetaStorage().put(REGISTRATION_KEY, registration);

        Log.info("Registered command: " + command.getClass().getName() + " " + registration);
    }

    /**
     * Analyzes the command class to determine what context types are used by @Command methods.
     */
    private ContextTypeInfo analyzeContextTypes(Class<?> clazz) {
        boolean hasPlayerContext = false;
        boolean hasGeneralContext = false;

        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Command.class)) {
                continue;
            }

            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length == 0) {
                continue;
            }

            Class<?> firstParam = paramTypes[0];
            if (HytalePlayerCommandContext.class.isAssignableFrom(firstParam)) {
                hasPlayerContext = true;
            } else if (HytaleCommandContext.class.isAssignableFrom(firstParam)) {
                hasGeneralContext = true;
            }
        }

        // Also check superclass methods
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && superclass != Object.class) {
            ContextTypeInfo superInfo = analyzeContextTypes(superclass);
            hasPlayerContext = hasPlayerContext || superInfo.hasPlayerContext;
            hasGeneralContext = hasGeneralContext || superInfo.hasGeneralContext;
        }

        return new ContextTypeInfo(hasPlayerContext, hasGeneralContext);
    }

    private static class ContextTypeInfo {
        final boolean hasPlayerContext;
        final boolean hasGeneralContext;

        ContextTypeInfo(boolean hasPlayerContext, boolean hasGeneralContext) {
            this.hasPlayerContext = hasPlayerContext;
            this.hasGeneralContext = hasGeneralContext;
        }

        boolean hasOnlyPlayerContext() {
            return hasPlayerContext && !hasGeneralContext;
        }

        boolean hasOnlyGeneralContext() {
            return hasGeneralContext && !hasPlayerContext;
        }
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
