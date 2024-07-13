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

package io.fairyproject.bukkit.command.map;

import io.fairyproject.bukkit.command.BukkitCommandExecutor;
import io.fairyproject.bukkit.command.sync.SyncCommandHandler;
import io.fairyproject.command.BaseCommand;
import io.fairyproject.data.MetaKey;
import io.fairyproject.data.MetaStorage;
import io.fairyproject.metadata.MetadataKey;
import io.fairyproject.metadata.MetadataMap;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

import java.util.Map;

@RequiredArgsConstructor
public class DefaultBukkitCommandMap implements BukkitCommandMap {

    public static final MetaKey<BukkitCommandExecutor> EXECUTOR_KEY = MetaKey.create("fairy:command-executor", BukkitCommandExecutor.class);

    private final CommandMap commandMap;
    private final Map<String, Command> knownCommands;
    private final SyncCommandHandler syncCommandHandler;

    @Override
    public void register(BaseCommand command) {
        if (this.isRegistered(command))
            throw new IllegalArgumentException("Command already registered");

        String[] commandNames = command.getCommandNames();
        BukkitCommandExecutor commandExecutor = new BukkitCommandExecutor(command, commandNames);
        String fallbackPrefix = commandExecutor.getFallbackPrefix();

        for (String name : commandNames) {
            if (this.isCommandNameRegistered(name, fallbackPrefix))
                throw new IllegalArgumentException(String.format("Command with name %s already registered", name));
        }


        command.getMetaStorage().put(EXECUTOR_KEY, commandExecutor);
        commandMap.register(fallbackPrefix, commandExecutor);
        syncCommandHandler.sync();
    }

    @Override
    public void unregister(BaseCommand command) {
        MetaStorage metaStorage = command.getMetaStorage();
        if (!this.isRegistered(command))
            throw new IllegalArgumentException("Command not registered");

        metaStorage.ifPresent(EXECUTOR_KEY, commandExecutor -> {
            String fallbackPrefix = commandExecutor.getFallbackPrefix();

            unregisterKnownCommand(fallbackPrefix, commandExecutor.getName());
            for (String alias : commandExecutor.getAliases()) {
                unregisterKnownCommand(fallbackPrefix, alias);
            }

            commandExecutor.unregister(commandMap);
            metaStorage.remove(EXECUTOR_KEY);
        });

        syncCommandHandler.sync();
    }

    private boolean isCommandNameRegistered(String name, String fallbackPrefix) {
        return knownCommands.containsKey(name) && knownCommands.containsKey(fallbackPrefix + ":" + name);
    }

    @Override
    public boolean isRegistered(BaseCommand command) {
        return command.getMetaStorage().contains(EXECUTOR_KEY);
    }

    private void unregisterKnownCommand(String fallbackPrefix, String alias) {
        knownCommands.remove(fallbackPrefix + ":" + alias);
        knownCommands.remove(alias);
    }
}
