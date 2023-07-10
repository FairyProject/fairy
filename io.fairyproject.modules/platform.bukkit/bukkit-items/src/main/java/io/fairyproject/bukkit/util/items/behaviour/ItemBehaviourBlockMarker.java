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

import com.cryptomorin.xseries.XSound;
import io.fairyproject.bukkit.listener.ListenerRegistry;
import io.fairyproject.bukkit.metadata.Metadata;
import io.fairyproject.bukkit.util.items.FairyItem;
import io.fairyproject.bukkit.util.items.FairyItemRef;
import io.fairyproject.bukkit.util.items.FairyItemRegistry;
import io.fairyproject.container.Autowired;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.registry.player.MCPlayerRegistry;
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

public class ItemBehaviourBlockMarker extends ItemBehaviourListener {

    private static final MetadataKey<String> METADATA = MetadataKey.createStringKey("fairy:block-marker");

    private final FairyItemRegistry fairyItemRegistry;
    private final MCPlayerRegistry mcPlayerRegistry;

    public ItemBehaviourBlockMarker(
            ListenerRegistry listenerRegistry,
            FairyItemRegistry fairyItemRegistry,
            MCPlayerRegistry mcPlayerRegistry) {
        super(listenerRegistry);
        this.fairyItemRegistry = fairyItemRegistry;
        this.mcPlayerRegistry = mcPlayerRegistry;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        Block block = event.getBlock();

        final ItemStack itemInHand = event.getItemInHand();
        if (!this.matches(player, itemInHand)) {
            return;
        }

        FairyItem item = this.fairyItemRegistry.get(itemInHand);
        if (item != this.item)
            return;

        Metadata.provideForBlock(block).put(METADATA, this.item.getName());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        final Block block = event.getBlock();

        final MetadataMap metadataMap = Metadata.provideForBlock(block);
        final String itemKey = metadataMap.getOrNull(METADATA);
        if (itemKey == null || !itemKey.equals(this.item.getName())) {
            return;
        }

        metadataMap.remove(METADATA);
        final FairyItem item = this.fairyItemRegistry.get(itemKey);
        if (item == null)
            return;

        this.addHandHeldItemDurability(player, 1);
        event.setCancelled(true);
        block.setType(Material.AIR);
        final ItemStack itemStack = item.provide(this.mcPlayerRegistry.findPlayerByUuid(player.getUniqueId()))
                .amount(1)
                .build();
        player.getWorld().dropItemNaturally(block.getLocation(), itemStack);
    }

    private void addHandHeldItemDurability(Player player, int amount) {
        ItemStack itemStack = player.getItemInHand();
        if (itemStack.getType().getMaxDurability() > 1) { // ensure item is not unbreakable
            itemStack.setDurability((short) (itemStack.getDurability() + amount));
            if (itemStack.getDurability() > itemStack.getType().getMaxDurability()) {
                player.setItemInHand(null);
                player.updateInventory();

                XSound.ENTITY_ITEM_BREAK.play(player);
            }
        }
    }

    @Override
    public boolean shouldFilterItemKey() {
        return false;
    }
}
