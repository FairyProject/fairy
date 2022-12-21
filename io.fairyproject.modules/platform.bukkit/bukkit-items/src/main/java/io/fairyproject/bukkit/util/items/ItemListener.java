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

import io.fairyproject.Fairy;
import io.fairyproject.bukkit.metadata.Metadata;
import io.fairyproject.container.Autowired;
import io.fairyproject.container.object.Obj;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.metadata.MetadataKey;
import io.fairyproject.metadata.MetadataMap;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

@Obj
public class ItemListener implements Listener {

    private static final MetadataKey<Boolean> METADATA = MetadataKey.createBooleanKey(Fairy.METADATA_PREFIX + "Item");

    @Autowired
    private FairyItemRegistry fairyItemRegistry;

//    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
//    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
//        Player player = event.getPlayer();
//
//        Item item = event.getItem();
//
//        MetadataMap metadataMap = Metadata.get(item).orElse(null);
//        if (metadataMap != null && metadataMap.has(METADATA)) {
//            return;
//        }
//
//        ItemStack pickupItemStack = item.getItemStack();
//        FairyItem fairyItem = this.fairyItemRegistry.get(pickupItemStack);
//        if (fairyItem == null)
//            return;
//
//        ItemStack itemStack = fairyItem.provide(MCPlayer.from(player))
//                .amount(pickupItemStack.getAmount())
//                .durability(pickupItemStack.getDurability())
//                .build();
//
//        item.setItemStack(itemStack);
//        Metadata.provide(item).put(METADATA, true);
//        event.setCancelled(true);
//    }

}
