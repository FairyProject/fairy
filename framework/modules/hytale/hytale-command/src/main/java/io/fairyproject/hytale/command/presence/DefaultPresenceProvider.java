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

package io.fairyproject.hytale.command.presence;

import com.hypixel.hytale.server.core.Message;
import io.fairyproject.command.MessageType;
import io.fairyproject.command.PresenceProvider;
import io.fairyproject.hytale.command.event.HytaleCommandContext;

/**
 * Default presence provider for Hytale commands using Hytale's Message API.
 */
public class DefaultPresenceProvider implements PresenceProvider<HytaleCommandContext> {

    private static final String COLOR_INFO = "#55FFFF";    // Aqua
    private static final String COLOR_WARN = "#FFAA00";    // Gold
    private static final String COLOR_ERROR = "#FF5555";   // Red

    @Override
    public Class<HytaleCommandContext> type() {
        return HytaleCommandContext.class;
    }

    @Override
    public void sendMessage(HytaleCommandContext commandContext, MessageType messageType, String... messages) {
        String color;
        switch (messageType) {
            case WARN:
                color = COLOR_WARN;
                break;
            case ERROR:
                color = COLOR_ERROR;
                break;
            default:
                color = COLOR_INFO;
                break;
        }

        for (String message : messages) {
            Message hytaleMessage = Message.raw(message).color(color);
            commandContext.getSender().sendMessage(hytaleMessage);
        }
    }
}
