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

package org.fairy.bukkit.util.items;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.collect.Lists;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.fairy.bean.*;
import org.fairy.bukkit.FairyBukkitPlatform;
import org.fairy.bukkit.reflection.ProtocolLibService;
import org.fairy.mc.PlaceholderEntry;
import org.fairy.locale.Locales;
import org.fairy.util.StringUtil;

import java.util.List;

@Service(name = "item:localization")
@ServiceDependency(dependencies = "protocollib", type = @DependencyType(ServiceDependencyType.SUB_DISABLE))
public class ItemLocalization {

    public static boolean PACKET_BASED_ITEM_LOCALIZATION = false;

    @Autowired
    private ProtocolLibService protocolLibService;

    @PostInitialize
    public void onPostInitialize() {
        PACKET_BASED_ITEM_LOCALIZATION = true;
        this.protocolLibService.manager().addPacketListener(new PacketAdapter(FairyBukkitPlatform.PLUGIN, PacketType.Play.Server.SET_SLOT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                final Player player = event.getPlayer();
                final PacketContainer packet = event.getPacket();
                final ItemStack itemStack = packet.getItemModifier().read(0);

                final ItemStack translatedItem = tryTranslate(player, itemStack);
                if (translatedItem != null) {
                    packet.getItemModifier().write(0, translatedItem);
                }
            }
        });
        this.protocolLibService.manager().addPacketListener(new PacketAdapter(FairyBukkitPlatform.PLUGIN, PacketType.Play.Server.WINDOW_ITEMS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                final Player player = event.getPlayer();
                final PacketContainer packet = event.getPacket();
                final ItemStack[] itemStacks = packet.getItemArrayModifier().read(0);

                boolean changed = false;
                for (int i = 0; i < itemStacks.length; i++) {
                    final ItemStack translatedItem = tryTranslate(player, itemStacks[i]);
                    if (translatedItem != null) {
                        itemStacks[i] = translatedItem;
                        changed = true;
                    }
                }

                if (changed) {
                    packet.getItemArrayModifier().write(0, itemStacks);
                }
            }
        });
    }

    private ItemStack tryTranslate(Player player, ItemStack itemStack) {
        final ImanityItem item = ImanityItem.getItemFromBukkit(itemStack);
        if (item != null) {
            itemStack = itemStack.clone(); // i don't know if there is any more better way...
            final ItemMeta itemMeta = itemStack.getItemMeta();

            if (item.getDisplayNameLocale() != null) {
                String name = Locales.translate(player, item.getDisplayNameLocale());
                for (PlaceholderEntry rv : item.getDisplayNamePlaceholders()) {
                    name = StringUtil.replace(name, rv.getTarget(), rv.getReplacement(player));
                }

                if (itemMeta.getDisplayName() != null) {
                    name += itemMeta.getDisplayName();
                }
                itemMeta.setDisplayName(name);
            }

            if (item.getDisplayLoreLocale() != null) {
                String lore = Locales.translate(player, item.getDisplayLoreLocale());
                for (PlaceholderEntry rv : item.getDisplayLorePlaceholders()) {
                    lore = StringUtil.replace(lore, rv.getTarget(), rv.getReplacement(player));
                }

                List<String> list = StringUtil.separateLines(lore, "\n");
                if (itemMeta.getLore() != null && !itemMeta.getLore().isEmpty()) {
                    list = Lists.newArrayList(list);
                    list.addAll(itemMeta.getLore());
                }
                itemMeta.setLore(list);
            }

            itemStack.setItemMeta(itemMeta);
            return itemStack;
        }

        return null;
    }

}
