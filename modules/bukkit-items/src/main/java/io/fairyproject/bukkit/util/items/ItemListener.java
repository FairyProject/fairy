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

package io.fairyproject.bukkit.util.items;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import io.fairyproject.Fairy;
import io.fairyproject.bukkit.metadata.Metadata;
import io.fairyproject.bean.Component;
import io.fairyproject.metadata.MetadataKey;
import io.fairyproject.metadata.MetadataMap;

@Component
public class ItemListener implements Listener {

    private static final MetadataKey<Boolean> METADATA = MetadataKey.createBooleanKey(Fairy.METADATA_PREFIX + "Item");

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();

        Item item = event.getItem();

        MetadataMap metadataMap = Metadata.get(item).orElse(null);
        if (metadataMap != null && metadataMap.has(METADATA)) {
            return;
        }

        ItemStack itemStack = item.getItemStack();
        ImanityItem imanityItem = ImanityItem.getItemFromBukkit(itemStack);

        if (imanityItem == null) {
            return;
        }

        ItemStack resultItem = imanityItem.get(player);
        resultItem.setAmount(itemStack.getAmount());
        resultItem.setDurability(itemStack.getDurability());

        item.setItemStack(resultItem);
        Metadata.provide(item).put(METADATA, true);
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();

        Action action = event.getAction();

        if (action == Action.PHYSICAL) {
            return;
        }

        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return;
        }

        ImanityItem imanityItem = ImanityItem.getItemFromBukkit(itemStack);

        if (imanityItem == null) {
            return;
        }

        if (imanityItem.getClickCallback() != null &&
                !imanityItem.getClickCallback().onClick(player, itemStack, action, event)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItemInHand();
        Block block = event.getBlock();

        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return;
        }

        ImanityItem imanityItem = ImanityItem.getItemFromBukkit(itemStack);

        if (imanityItem == null) {
            return;
        }

        if (imanityItem.getPlaceCallback() != null &&
                !imanityItem.getPlaceCallback().onPlace(player, itemStack, block, event)) {
            event.setCancelled(true);
        }
    }

}
