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

package io.fairyproject.bukkit.events;

import io.fairyproject.event.EventFilter;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.server.PluginEvent;
import org.bukkit.event.world.WorldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

public class BukkitEventFilter {

    public static final EventFilter<Event, ?> ALL = EventFilter.from(Event.class, null, null);
    public static final EventFilter<BlockEvent, Block> BLOCK = EventFilter.from(BlockEvent.class, Block.class, BlockEvent::getBlock);
    public static final EventFilter<PlayerEvent, Player> PLAYER = EventFilter.from(PlayerEvent.class, Player.class, PlayerEvent::getPlayer);
    public static final EventFilter<EntityEvent, Entity> ENTITY = EventFilter.from(EntityEvent.class, Entity.class, EntityEvent::getEntity);
    public static final EventFilter<WorldEvent, World> WORLD = EventFilter.from(WorldEvent.class, World.class, WorldEvent::getWorld);
    public static final EventFilter<InventoryEvent, Inventory> INVENTORY = EventFilter.from(InventoryEvent.class, Inventory.class, InventoryEvent::getInventory);
    public static final EventFilter<PluginEvent, Plugin> PLUGIN = EventFilter.from(PluginEvent.class, Plugin.class, PluginEvent::getPlugin);

}
