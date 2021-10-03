/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
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

package org.fairy.bukkit.command;

import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.fairy.bean.Autowired;
import org.fairy.bean.Component;
import org.fairy.bukkit.FairyBukkitPlatform;
import org.fairy.bukkit.command.event.BukkitCommandEvent;
import org.fairy.bukkit.events.NetworkCommandExecuteEvent;
import org.fairy.bukkit.events.PostServicesInitialEvent;
import org.fairy.bukkit.listener.events.Events;
import org.fairy.command.CommandEvent;
import org.fairy.command.CommandProvider;
import org.fairy.command.CommandService;
import org.fairy.redis.server.ImanityServer;
import org.fairy.task.Task;
import org.fairy.util.AccessUtil;

import java.lang.reflect.Field;

@Component
public class CommandListener implements Listener {

    @Autowired
    private CommandService commandService;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommandPreProcess(PlayerCommandPreprocessEvent event) {
        final String command = event.getMessage().substring(1);

        CommandMap.parameters.put(event.getPlayer().getUniqueId(), command.split(" "));
        CommandEvent commandEvent = new BukkitCommandEvent(event.getPlayer(), command);

        if (this.commandService.evalCommand(commandEvent)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onConsoleCommand(ServerCommandEvent event) {
        CommandEvent commandEvent = new BukkitCommandEvent(event.getSender(), event.getCommand());

        if (this.commandService.evalCommand(commandEvent)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPostServiceInitial(PostServicesInitialEvent event) {
        this.commandService.withProvider(new CommandProvider() {
            @Override
            public boolean hasPermission(Object user, String permission) {
                if (user instanceof CommandSender) {
                    return ((CommandSender) user).hasPermission(permission);
                }
                throw new UnsupportedOperationException();
            }

            @Override
            public void execute(String command, String context, String executor, ImanityServer server) {
                NetworkCommandExecuteEvent event = new NetworkCommandExecuteEvent(server, command, context, executor);
                Events.call(event);

                if (event.isCancelled()) {
                    return;
                }

                command = event.getCommand();
                FairyBukkitPlatform.PLUGIN.getServer().dispatchCommand(FairyBukkitPlatform.PLUGIN.getServer().getConsoleSender(), command);
            }
        });

        Task.runMainLater(() -> {
            try {
                // Command map field (we have to use reflection to get this)
                final Field commandMapField = FairyBukkitPlatform.PLUGIN.getServer().getClass().getDeclaredField("commandMap");
                AccessUtil.setAccessible(commandMapField);

                final Object oldCommandMap = commandMapField.get(FairyBukkitPlatform.PLUGIN.getServer());
                final CommandMap newCommandMap = new CommandMap(FairyBukkitPlatform.PLUGIN.getServer());

                // Start copying the knownCommands field over
                // (so any commands registered before we hook in are kept)
                final Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");

                // The knownCommands field is final,
                // so to be able to set it in the new command map we have to remove it.
                AccessUtil.setAccessible(knownCommandsField);

                knownCommandsField.set(newCommandMap, knownCommandsField.get(oldCommandMap));
                // End copying the knownCommands field over

                commandMapField.set(FairyBukkitPlatform.PLUGIN.getServer(), newCommandMap);
            } catch (final Exception e) {
                // Shouldn't happen, so we can just
                // printout the exception (and do nothing else)
            }
        }, 5L);
    }

}
