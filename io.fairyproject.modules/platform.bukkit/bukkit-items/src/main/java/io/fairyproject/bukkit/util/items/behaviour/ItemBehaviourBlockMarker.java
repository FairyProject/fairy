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

package io.fairyproject.bukkit.util.items.behaviour;

import io.fairyproject.Fairy;
import io.fairyproject.bukkit.util.items.ImanityItem;
import io.fairyproject.metadata.MetadataKey;
import io.fairyproject.metadata.MetadataMap;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import io.fairyproject.bukkit.metadata.Metadata;

public class ItemBehaviourBlockMarker extends ItemBehaviourListener {

    private static final MetadataKey<String> METADATA = MetadataKey.createStringKey(Fairy.METADATA_PREFIX + "block-marker");

    @Override
    public void init(ImanityItem item) {
        if (!item.getType().isBlock()) {
            throw new IllegalArgumentException("Material " + item.getType() + " is not block! but it's trying to register place event!");
        }
        super.init(item);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        Block block = event.getBlock();

        final ItemStack itemInHand = event.getItemInHand();
        if (!this.matches(player, itemInHand)) {
            return;
        }

        final String itemKey = ImanityItem.getItemKeyFromBukkit(itemInHand);
        if (itemKey == null || !itemKey.equals(this.item.getId())) {
            return;
        }
        Metadata.provideForBlock(block).put(METADATA, itemKey);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        final Block block = event.getBlock();

        final MetadataMap metadataMap = Metadata.provideForBlock(block);
        final String itemKey = metadataMap.getOrNull(METADATA);
        if (itemKey == null || !itemKey.equals(this.item.getId())) {
            return;
        }

        metadataMap.remove(METADATA);
        final ImanityItem item = ImanityItem.getItem(itemKey);
        if (item == null) {
            return;
        }
        event.setCancelled(true);
        block.setType(Material.AIR);
        final ItemStack itemStack = item.get(player);
        itemStack.setAmount(1);
        player.getWorld().dropItemNaturally(block.getLocation(), itemStack);
    }

    @Override
    public boolean shouldFilterItemKey() {
        return false;
    }
}
