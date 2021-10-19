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

package io.fairyproject.command;

import io.fairyproject.command.argument.ArgProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class CommandContext {

    private final Map<ArgProperty<?>, Object> properties;
    private String[] args;
    private PresenceProvider presenceProvider;

    public CommandContext(String[] args) {
        this.args = args;
        this.properties = new HashMap<>();
    }

    public String name() {
        return "default-executor";
    }

    public <T> void addProperty(ArgProperty<T> key, T value) {
        this.properties.put(key, value);
    }

    public boolean hasProperty(ArgProperty<?> key) {
        return this.properties.containsKey(key);
    }

    public <T> T getProperty(ArgProperty<T> property) {
        return property.getType().cast(this.properties.getOrDefault(property, null));
    }

    public <T extends CommandContext> T cast(Class<T> type) {
        if (!type.isInstance(this)) {
            throw new UnsupportedOperationException();
        }
        return type.cast(this);
    }

    public final void sendMessage(MessageType messageType, String message) {
        this.presenceProvider.sendMessage(this, messageType, message);
    }

    public boolean shouldExecute(CommandMeta meta, String[] arguments) {
        return true;
    }

    public boolean hasPermission(String permission) {
        return true;
    }
}
