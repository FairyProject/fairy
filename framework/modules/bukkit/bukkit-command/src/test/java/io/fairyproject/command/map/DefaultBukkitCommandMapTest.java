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

package io.fairyproject.command.map;

import be.seeseemelk.mockbukkit.MockBukkit;
import io.fairyproject.bukkit.command.BukkitCommandExecutor;
import io.fairyproject.bukkit.command.map.DefaultBukkitCommandMap;
import io.fairyproject.bukkit.command.sync.SyncCommandHandler;
import io.fairyproject.bukkit.plugin.impl.RootJavaPluginIdentifier;
import io.fairyproject.bukkit.plugin.impl.SpecifyJavaPluginIdentifier;
import io.fairyproject.command.BaseCommand;
import io.fairyproject.metadata.MetadataMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultBukkitCommandMapTest {

    private DefaultBukkitCommandMap defaultBukkitCommandMap;
    private SyncCommandHandler syncCommandHandler;
    private CommandMap commandMap;
    private Map<String, Command> knownCommands;
    private BaseCommand command;
    private MetadataMap metadata;

    @BeforeEach
    void setUp() {
        commandMap = Mockito.mock(CommandMap.class);
        command = Mockito.mock(BaseCommand.class);
        metadata = MetadataMap.create();
        Mockito.when(command.getCommandNames()).thenReturn(new String[]{"test", "test2"});
        Mockito.when(command.getMetadata()).thenReturn(metadata);

        knownCommands = new HashMap<>();
        Mockito.when(commandMap.register(Mockito.anyString(), Mockito.any()))
                .thenAnswer(invocation -> {
                    String fallback = invocation.getArgument(0);
                    Command command = invocation.getArgument(1);

                    knownCommands.put(fallback + ":" + command.getName(), command);
                    knownCommands.put(command.getName(), command);

                    for (String alias : command.getAliases()) {
                        knownCommands.put(fallback + ":" + alias, command);
                        knownCommands.put(alias, command);
                    }

                    command.register(commandMap);
                    return false;
                });

        syncCommandHandler = Mockito.mock(SyncCommandHandler.class);
        defaultBukkitCommandMap = new DefaultBukkitCommandMap(commandMap, knownCommands, syncCommandHandler);

        MockBukkit.mock();
        RootJavaPluginIdentifier.getInstance().addFirst(new SpecifyJavaPluginIdentifier(MockBukkit.createMockPlugin("test-plugin")));
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
        RootJavaPluginIdentifier.clearInstance();
    }

    @Test
    void register() {
        defaultBukkitCommandMap.register(command);

        BukkitCommandExecutor executor = metadata.getOrThrow(DefaultBukkitCommandMap.EXECUTOR_KEY);
        assertTrue(knownCommands.containsKey("test"));
        assertTrue(knownCommands.containsKey("test-plugin:test"));
        assertTrue(knownCommands.containsKey("test2"));
        assertTrue(knownCommands.containsKey("test-plugin:test2"));
        assertTrue(executor.isRegistered());
        Mockito.verify(syncCommandHandler).sync();
    }

    @Test
    void registerShouldAvoidDuplicateInstance() {
        defaultBukkitCommandMap.register(command);

        Assertions.assertThrows(IllegalArgumentException.class, () -> defaultBukkitCommandMap.register(command));
    }

    @Test
    void registerShouldAvoidDuplicateNames() {
        BaseCommand secondCommand = Mockito.mock(BaseCommand.class);
        Mockito.when(secondCommand.getCommandNames()).thenReturn(new String[]{"test3", "test2"});
        Mockito.when(secondCommand.getMetadata()).thenReturn(MetadataMap.create());

        defaultBukkitCommandMap.register(command);

        Assertions.assertThrows(IllegalArgumentException.class, () -> defaultBukkitCommandMap.register(secondCommand));
    }

    @Test
    void unregister() {
        defaultBukkitCommandMap.register(command);
        BukkitCommandExecutor executor = metadata.getOrThrow(DefaultBukkitCommandMap.EXECUTOR_KEY);
        defaultBukkitCommandMap.unregister(command);

        assertTrue(knownCommands.isEmpty());
        assertFalse(metadata.has(DefaultBukkitCommandMap.EXECUTOR_KEY));
        assertFalse(executor.isRegistered());
        Mockito.verify(syncCommandHandler, Mockito.times(2)).sync();
    }

    @Test
    void isRegistered() {
        defaultBukkitCommandMap.register(command);

        assertTrue(defaultBukkitCommandMap.isRegistered(command));

        defaultBukkitCommandMap.unregister(command);

        assertFalse(defaultBukkitCommandMap.isRegistered(command));
    }
}