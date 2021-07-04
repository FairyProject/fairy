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

package org.fairy.command;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

@Getter
public class CommandEvent {

    @Nullable
    private final Object user;

    private final String command;
    @Setter
    private PresenceProvider<?> presenceProvider;

    public CommandEvent(@Nullable Object user, String command) {
        this.user = user;
        this.command = command;
    }

    public String name() {
        return "default-executor";
    }

    public <T extends CommandEvent> T cast(Class<T> type) {
        if (!type.isInstance(this)) {
            throw new UnsupportedOperationException();
        }
        return (T) this;
    }

    public final void sendUsage(String usage) {
        this.presenceProvider.sendUsage0(this, usage);
    }

    public final void sendError(Throwable throwable) {
        this.presenceProvider.sendError0(this, throwable);
    }

    public final void sendNoPermission() {
        this.presenceProvider.sendNoPermission0(this);
    }

    public final void sendInternalError(String message) {
        this.presenceProvider.sendInternalError0(this, message);
    }

    public boolean shouldExecute(CommandMeta meta, String[] arguments) {
        return true;
    }

}
