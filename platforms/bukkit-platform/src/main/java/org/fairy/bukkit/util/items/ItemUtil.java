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

import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@UtilityClass
public class ItemUtil {

    public static String serializeItemStack(@Nullable ItemStack item) {
        StringBuilder builder = new StringBuilder();

        if (item == null) {
            return "null";
        }

        String type = item.getType().name();
        builder.append("t@").append(type);

        if (item.getDurability() != 0) {
            String isDurability = String.valueOf(item.getDurability());
            builder.append(":d@").append(isDurability);
        }

        if (item.getAmount() != 1) {
            String isAmount = String.valueOf(item.getAmount());
            builder.append(":a@").append(isAmount);
        }

        Map<Enchantment, Integer> enchantments = item.getEnchantments();

        if (enchantments.size() > 0) {
            for (Map.Entry<Enchantment, Integer> enchantment : enchantments.entrySet()) {
                builder.append(":e@").append(enchantment.getKey().getName()).append("@").append(enchantment.getValue());
            }
        }

        if (item.hasItemMeta()) {
            ItemMeta itemMeta = item.getItemMeta();

            if (itemMeta instanceof EnchantmentStorageMeta) {
                for (Map.Entry<Enchantment, Integer> enchantment : ((EnchantmentStorageMeta) itemMeta).getStoredEnchants().entrySet()) {
                    builder.append(":e@").append(enchantment.getKey().getName()).append("@").append(enchantment.getValue());
                }
            }

            if (itemMeta.hasDisplayName()) {
                builder.append(":dn@").append(itemMeta.getDisplayName());
            }

            if (itemMeta.hasLore()) {
                builder.append(":l@").append(itemMeta.getLore());
            }
        }

        return builder.toString();
    }

    public static ItemStack deserializeItemStack(String in) {
        try {
            ItemStack item = null;
            ItemMeta meta = null;

            boolean applyMeta = false;

            if (in.equals("null")) {
                return new ItemStack(Material.AIR);
            }

            String[] split = in.split(":");

            for (String itemInfo : split) {
                String[] itemAttribute = itemInfo.split("@");
                String s2 = itemAttribute[0];

                switch (s2) {
                    case "t": {
                        item = new ItemStack(Material.getMaterial(itemAttribute[1].toUpperCase()));
                        meta = item.getItemMeta();
                        break;
                    }
                    case "d": {
                        if (item != null) {
                            item.setDurability(Short.parseShort(itemAttribute[1]));
                            break;
                        }
                        break;
                    }
                    case "a": {
                        if (item != null) {
                            item.setAmount(Integer.parseInt(itemAttribute[1]));
                            break;
                        }
                        break;
                    }
                    case "e": {
                        if (item != null) {
                            if (meta instanceof EnchantmentStorageMeta) {
                                applyMeta = true;
                                ((EnchantmentStorageMeta) meta).addStoredEnchant(Enchantment.getByName(itemAttribute[1]),
                                        Integer.parseInt(itemAttribute[2]),
                                        true
                                );
                            } else {
                                item.addUnsafeEnchantment(
                                        Enchantment.getByName(itemAttribute[1]),
                                        Integer.parseInt(itemAttribute[2])
                                );
                            }
                            break;
                        }
                        break;
                    }
                    case "dn": {
                        if (meta != null) {
                            meta.setDisplayName(itemAttribute[1]);
                            break;
                        }
                        break;
                    }
                    case "l": {
                        itemAttribute[1] = itemAttribute[1].replace("[", "");
                        itemAttribute[1] = itemAttribute[1].replace("]", "");
                        List<String> lore = Arrays.asList(itemAttribute[1].split(","));

                        for (int x = 0; x < lore.size(); ++x) {
                            String s = lore.get(x);

                            if (s != null) {
                                if (s.toCharArray().length != 0) {
                                    if (s.charAt(0) == ' ') {
                                        s = s.replaceFirst(" ", "");
                                    }

                                    lore.set(x, s);
                                }
                            }
                        }

                        if (meta != null) {
                            meta.setLore(lore);
                            break;
                        }

                        break;
                    }
                }
            }

            if (meta != null && (applyMeta || meta.hasDisplayName() || meta.hasLore())) {
                item.setItemMeta(meta);
            }

            return item;
        } catch (Throwable throwable) {
            throw new RuntimeException("An error thrown while deserializing item " + in, throwable);
        }
    }

}
