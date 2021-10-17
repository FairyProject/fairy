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

package io.fairyproject.bukkit.command.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemUtil {

    public static ItemData[] repeat(final Material material, final int times) {
        return repeat(material, (byte) 0, times);
    }

    public static ItemData[] repeat(final Material material, final byte data, final int times) {
        final ItemData[] itemData = new ItemData[times];

        for (int i = 0; i < times; i++) {
            itemData[i] = new ItemData(material, data);
        }

        return itemData;

    }

    public static ItemData[] swords() {
        final List<ItemData> data = new ArrayList<>();

        for (final SwordType at : SwordType.values()) {
            data.add(new ItemData(Material.valueOf(at.name() + "_SWORD"), (short) 0));
        }

        return data.toArray(new ItemData[data.size()]);
    }

    public static ItemStack get(final String input, final int amount) {
        final ItemStack item = get(input);

        if (item != null) {
            item.setAmount(amount);
        }

        return item;
    }

    public static ItemStack get(final String input) {
        if (NumberUtil.isInteger(input))
            return new ItemStack(Material.getMaterial(Integer.parseInt(input)));

        if (input.contains(":")) {
            final String[] names = input.split(":");
            if (NumberUtil.isShort(names[1])) {
                if (NumberUtil.isInteger(names[0]))
                    return new ItemStack(Material.getMaterial(Integer.parseInt(names[0])), 1,
                            Short.parseShort(names[1]));
                else {
                    Material material = Material.getMaterial(names[0].toLowerCase());
                    if (material == null) {
                        return null;
                    }

                    return new ItemStack(material, 1, Short.parseShort(names[1]));
                }
            } else
                return null;
        }

        Material material = Material.getMaterial(input);
        if (material == null) {
            return null;
        }

        return new ItemStack(material);
    }

    public static String getName(final ItemStack item) {
        if (item.getDurability() != 0) {
            String reflectedName = BukkitReflection.getItemStackName(item);

            if (reflectedName != null) {
                if (reflectedName.contains(".")) {
                    reflectedName = WordUtils.capitalize(item.getType().toString().toLowerCase().replace("_", " "));
                }

                return reflectedName;
            }
        }

        final String string = item.getType().toString().replace("_", " ");
        final char[] chars = string.toLowerCase().toCharArray();
        boolean found = false;

        for (int i = 0; i < chars.length; i++) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            } else if (Character.isWhitespace(chars[i]) || chars[i] == '.' || chars[i] == '\'') {
                found = false;
            }
        }

        return String.valueOf(chars);
    }

    public enum ArmorType {
        DIAMOND, IRON, GOLD, LEATHER
    }

    public enum SwordType {
        DIAMOND, IRON, GOLD, STONE
    }

    @Getter
    @AllArgsConstructor
    public static class ItemData {

        private final Material material;
        private final short data;

        public String getName() {
            return ItemUtil.getName(toItemStack());
        }

        public boolean matches(final ItemStack item) {
            return item != null && item.getType() == material && item.getDurability() == data;
        }

        public ItemStack toItemStack() {
            return new ItemStack(material, 1, data);
        }

    }

}
