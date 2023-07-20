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

package io.fairyproject.bukkit.menu.sequence.condition;

import io.fairyproject.bukkit.events.BukkitEventFilter;
import io.fairyproject.bukkit.menu.Menu;
import io.fairyproject.bukkit.menu.event.MenuCloseEvent;
import io.fairyproject.bukkit.menu.sequence.MenuNode;
import io.fairyproject.event.EventNode;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;

public class CloseCondition implements Condition {
    @Override
    public void setup(MenuNode menuNode) {
        Menu menu = menuNode.getMenu();
        EventNode<Event> parent = menu.getEventNode();

        EventNode<PlayerEvent> eventNode = EventNode.value("fairy:menu:close-condition", BukkitEventFilter.PLAYER, player -> menu.getPlayer() == player);
        eventNode.addListener(MenuCloseEvent.class, event -> onMenuClose(event, menuNode));

        parent.addChild(eventNode);
        menu.bind(eventNode);
    }

    private void onMenuClose(MenuCloseEvent event, MenuNode menuNode) {
        menuNode.next(event.getPlayer(), this);
    }
}
