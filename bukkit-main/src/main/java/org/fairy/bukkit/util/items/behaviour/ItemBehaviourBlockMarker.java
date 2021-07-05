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

package org.fairy.bukkit.util.items.behaviour;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.fairy.bukkit.metadata.Metadata;
import org.fairy.bukkit.util.items.ImanityItem;
import org.fairy.metadata.MetadataKey;
import org.fairy.metadata.MetadataMap;

public class ItemBehaviourBlockMarker extends ItemBehaviourListener {

    private static final MetadataKey<String> METADATA = MetadataKey.createStringKey("fairy:block-marker");

    @Override
    public void init(ImanityItem item) {
        if (!item.getType().isBlock()) {
            throw new IllegalArgumentException("Material " + item.getType() + " is not block! but it's trying to register place event!");
        }
        super.init(item);
    }

    @EventHandler
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

    @EventHandler
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
        block.getDrops().clear();
        block.getDrops().add(item.get(player));
    }

    @Override
    public boolean shouldFilterItemKey() {
        return false;
    }
}
