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

package io.fairyproject.bukkit.command.parameters;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import io.fairyproject.command.CommandEvent;
import io.fairyproject.command.parameter.ParameterHolder;

import java.util.List;
import java.util.Set;

public abstract class BukkitParameterHolder<T> implements ParameterHolder<T> {

    @Override
    public final T transform(CommandEvent commandEvent, String source) {
        if (commandEvent.getUser() instanceof CommandSender) {
            return this.transform((CommandSender) commandEvent.getUser(), source);
        }

        throw new UnsupportedOperationException();
    }

    public abstract T transform(CommandSender sender, String source);

    public final List<String> tabComplete(Object user, Set<String> flags, String source) {
        if (user instanceof Player) {
            return this.tabComplete((Player) user, flags, source);
        }

        throw new UnsupportedOperationException();
    }

    public abstract List<String> tabComplete(Player player, Set<String> flags, String source);

}
