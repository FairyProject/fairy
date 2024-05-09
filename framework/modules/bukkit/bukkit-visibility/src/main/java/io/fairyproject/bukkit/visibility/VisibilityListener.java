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

package io.fairyproject.bukkit.visibility;

import io.fairyproject.bukkit.events.player.PlayerPostJoinEvent;
import io.fairyproject.bukkit.listener.RegisterAsListener;
import io.fairyproject.container.Autowired;
import io.fairyproject.container.InjectableComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;

import java.util.Collection;

@InjectableComponent
@RegisterAsListener
public class VisibilityListener implements Listener {

    @Autowired
    private VisibilityService visibilityService;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerPostJoinEvent event) {
        this.visibilityService.update(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTabComplete(PlayerChatTabCompleteEvent event) {
        final String token = event.getLastToken();
        final Collection<String> completions = event.getTabCompletions();
        completions.clear();
        for (final Player target : Bukkit.getOnlinePlayers()) {
            if (!this.visibilityService.treatAsOnline(target, event.getPlayer())) {
                continue;
            }
            if (!target.getName().toLowerCase().startsWith(token.toLowerCase())) {
                continue;
            }
            completions.add(target.getName());
        }
    }
}
