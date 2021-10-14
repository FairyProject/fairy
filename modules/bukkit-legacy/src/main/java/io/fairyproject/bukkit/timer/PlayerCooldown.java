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

package io.fairyproject.bukkit.timer;

import com.github.benmanes.caffeine.cache.RemovalCause;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import io.fairyproject.bukkit.listener.events.Events;
import io.fairyproject.util.Cooldown;

import java.util.function.BiConsumer;

public class PlayerCooldown extends Cooldown<Player> {

    public PlayerCooldown(long defaultCooldown, Plugin plugin) {
        this(defaultCooldown, null, plugin);
    }

    public PlayerCooldown(long defaultCooldown, BiConsumer<Player, RemovalCause> removalListener, Plugin plugin) {
        super(defaultCooldown, removalListener);

        Events.subscribe(PlayerQuitEvent.class)
                .listen((subscription, event) -> this.removeCooldown(event.getPlayer()))
                .build(plugin);
    }

}
