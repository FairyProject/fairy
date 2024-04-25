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

package io.fairyproject.bukkit.command.presence;

import io.fairyproject.bukkit.command.event.BukkitCommandContext;
import io.fairyproject.command.MessageType;
import io.fairyproject.command.PresenceProvider;
import org.bukkit.ChatColor;

public class DefaultPresenceProvider implements PresenceProvider<BukkitCommandContext> {

    @Override
    public Class<BukkitCommandContext> type() {
        return BukkitCommandContext.class;
    }

    @Override
    public void sendMessage(BukkitCommandContext commandContext, MessageType messageType, String... messages) {
        switch (messageType) {
            case INFO:
            default:
                for (String message : messages) {
                    commandContext.getSender().sendMessage(ChatColor.AQUA + message);
                }
                break;
            case WARN:
                for (String message : messages) {
                    commandContext.getSender().sendMessage(ChatColor.GOLD + message);
                }
                break;
            case ERROR:
                for (String message : messages) {
                    commandContext.getSender().sendMessage(ChatColor.RED + message);
                }
                break;

        }
    }
}
