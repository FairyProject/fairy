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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.fairy.bean.*;
import org.fairy.bukkit.Imanity;
import org.fairy.bukkit.reflection.ProtocolLibService;
import org.fairy.bukkit.util.LocaleRV;
import org.fairy.locale.Locales;
import org.fairy.util.StringUtil;

@Service(name = "item:localization")
@ServiceDependency(dependencies = "protocollib", type = @DependencyType(ServiceDependencyType.SUB_DISABLE))
public class ItemLocalization {

    public static boolean PACKET_BASED_ITEM_LOCALIZATION = false;

    @Autowired
    private ProtocolLibService protocolLibService;

    @PostInitialize
    public void onPostInitialize() {
        PACKET_BASED_ITEM_LOCALIZATION = true;
        this.protocolLibService.manager().addPacketListener(new PacketAdapter(Imanity.PLUGIN, PacketType.Play.Server.SET_SLOT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                final Player player = event.getPlayer();
                final PacketContainer packet = event.getPacket();
                final ItemStack itemStack = packet.getItemModifier().read(0);

                final ImanityItem item = ImanityItem.getItemFromBukkit(itemStack);
                if (item != null) {
                    System.out.println(item.getId());
                    final ItemMeta itemMeta = itemStack.getItemMeta();

                    if (item.getDisplayNameLocale() != null) {
                        String name = Locales.translate(player, item.getDisplayNameLocale());
                        for (LocaleRV rv : item.getDisplayNamePlaceholders()) {
                            name = StringUtil.replace(name, rv.getTarget(), rv.getReplacement(player));
                        }

                        itemMeta.setDisplayName(name);
                    }

                    if (item.getDisplayLoreLocale() != null) {
                        String lore = Locales.translate(player, item.getDisplayLoreLocale());
                        for (LocaleRV rv : item.getDisplayLorePlaceholders()) {
                            lore = StringUtil.replace(lore, rv.getTarget(), rv.getReplacement(player));
                        }

                        itemMeta.setLore(StringUtil.separateLines(lore, "\n"));
                    }

                    itemStack.setItemMeta(itemMeta);
                    packet.getItemModifier().write(0, itemStack);
                }
            }
        });
    }

}
