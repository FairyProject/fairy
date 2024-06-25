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

package io.fairyproject.bukkit.util.items.impl;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import io.fairyproject.bukkit.nbt.NBTKey;
import io.fairyproject.bukkit.nbt.NBTModifier;
import io.fairyproject.bukkit.util.items.ItemBuilder;
import io.fairyproject.bukkit.util.items.util.ItemBuilderUtil;
import io.fairyproject.util.CC;
import io.fairyproject.util.ConditionUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings({"deprecation", "unused"})
public class ItemBuilderImpl implements ItemBuilder {

    private ItemStack itemStack;

    public ItemBuilderImpl(final XMaterial mat) {
        itemStack = mat.parseItem();
    }

    public ItemBuilderImpl(final ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public ItemBuilderImpl amount(final int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    @Override
    public ItemBuilder name(Locale locale, Component name) {
        return this.editMeta(meta -> ItemBuilderUtil.setDisplayName(meta, locale, name));
    }

    @Override
    public ItemBuilder lores(Locale locale, Iterable<Component> lores) {
        return this.editMeta(meta -> {
            List<Component> loreList = ItemBuilderUtil.getLore(meta);
            if (loreList == null)
                loreList = new ArrayList<>();

            for (Component s : lores) {
                loreList.add(s);
            }

            ItemBuilderUtil.setLore(meta, locale, loreList);
        });
    }

    @Override
    public ItemBuilder lores(Locale locale, Component... lores) {
        return this.lores(locale, Arrays.asList(lores));
    }

    @Override
    public ItemBuilderImpl name(final String name) {
        return this.editMeta(meta -> meta.setDisplayName(CC.translate(name)));
    }

    @Override
    public ItemBuilderImpl lore(final Iterable<String> lore) {
        return this.editMeta(meta -> {
            List<String> loreList = meta.getLore();
            if (loreList == null)
                loreList = new ArrayList<>();

            for (String s : lore) {
                loreList.add(CC.translate(s));
            }

            meta.setLore(loreList);
        });
    }

    @Override
    public ItemBuilderImpl lore(final String... lore) {
        return this.lore(Arrays.asList(lore));
    }

    @Override
    public ItemBuilderImpl unbreakable(final boolean unbreakable) {
        return this.editMeta(meta -> meta.setUnbreakable(unbreakable));
    }

    @Override
    public ItemBuilderImpl durability(final int durability) {
        itemStack.setDurability((short) -(durability - itemStack.getType().getMaxDurability()));
        return this;
    }

    @Override
    public ItemBuilderImpl data(final int data) {
        itemStack.setDurability((short) data);
        return this;
    }

    @Override
    public ItemBuilderImpl enchantment(final XEnchantment enchantment, final int level) {
        Enchantment enchant = enchantment.getEnchant();
        ConditionUtils.notNull(enchant, enchantment.name() + " doesn't seems to be supported in current version.");

        itemStack.addUnsafeEnchantment(enchant, level);
        return this;
    }

    @Override
    public ItemBuilderImpl enchantment(final XEnchantment enchantment) {
        Enchantment enchant = enchantment.getEnchant();
        ConditionUtils.notNull(enchant, enchantment.name() + " doesn't seems to be supported in current version.");

        itemStack.addUnsafeEnchantment(enchant, 1);
        return this;
    }

    @Override
    public ItemBuilderImpl type(final XMaterial material) {
        Material bukkitMaterial = material.parseMaterial();
        ConditionUtils.notNull(bukkitMaterial, material.name() + " doesn't seems to be supported in current version.");

        itemStack.setType(bukkitMaterial);
        return this;
    }

    @Override
    public ItemBuilderImpl clearLore() {
        return this.editMeta(meta -> meta.setLore(Collections.emptyList()));
    }

    @Override
    public ItemBuilderImpl clearEnchantments() {
        for (final Enchantment e : itemStack.getEnchantments().keySet()) {
            itemStack.removeEnchantment(e);
        }
        return this;
    }

    @Override
    public ItemBuilderImpl color(final Color color) {
        if (itemStack.getType() == Material.LEATHER_BOOTS || itemStack.getType() == Material.LEATHER_CHESTPLATE || itemStack.getType() == Material.LEATHER_HELMET
                || itemStack.getType() == Material.LEATHER_LEGGINGS) {
            return this.editMeta(LeatherArmorMeta.class, meta -> meta.setColor(color));
        } else
            throw new IllegalArgumentException("color() only applicable for leather armor!");
    }

    @Override
    public ItemBuilderImpl skull(String owner) {
        if (XMaterial.PLAYER_HEAD.isSimilar(itemStack)) {
            return editMeta(itemMeta -> XSkull.of(itemMeta).profile(Profileable.username(owner)).apply());
        } else {
            throw new IllegalArgumentException("skull() only applicable for human skull item!");
        }
    }

    @Override
    public ItemBuilderImpl skull(Player owner) {
        if (XMaterial.PLAYER_HEAD.isSimilar(itemStack)) {
            return editMeta(SkullMeta.class, itemMeta -> ItemBuilderUtil.setSkullWithPlayer(itemMeta, owner));
        } else {
            throw new IllegalArgumentException("skull() only applicable for human skull item!");
        }
    }

    @Override
    public ItemBuilderImpl shiny() {
        return this.editMeta(meta -> {
            meta.addEnchant(Enchantment.PROTECTION_FIRE, 1, false);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        });
    }

    @Override
    @Deprecated
    public ItemBuilderImpl tag(Object value, String... key) {
        return tag(NBTKey.create(key), value);
    }

    @Override
    public ItemBuilderImpl tag(NBTKey key, Object value) {
        this.itemStack = NBTModifier.get().setTag(this.itemStack, key, value);
        return this;
    }

    @Override
    public ItemBuilderImpl itemFlag(ItemFlag itemFlag) {
        return this.editMeta(meta -> meta.addItemFlags(itemFlag));
    }

    @Override
    public ItemBuilderImpl removeItemFlag(ItemFlag itemFlag) {
        return this.editMeta(meta -> {
            if (meta.hasItemFlag(itemFlag)) {
                meta.removeItemFlags(itemFlag);
            }
        });
    }

    @Override
    public ItemBuilderImpl editMeta(Consumer<ItemMeta> consumer) {
        ItemMeta meta = itemStack.getItemMeta();
        ConditionUtils.notNull(meta, "ItemMeta is null!");

        try {
            consumer.accept(meta);
        } finally {
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    @Override
    public <T extends ItemMeta> ItemBuilderImpl editMeta(Class<T> metaClass, Consumer<T> consumer) {
        ItemMeta meta = itemStack.getItemMeta();
        ConditionUtils.notNull(meta, "ItemMeta is null!");
        ConditionUtils.is(metaClass.isInstance(meta), "ItemMeta is not instance of " + metaClass.getName());

        try {
            consumer.accept(metaClass.cast(meta));
        } finally {
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    @Override
    public ItemBuilderImpl editItemStack(Consumer<ItemStack> consumer) {
        consumer.accept(this.itemStack);
        return this;
    }

    @Override
    public ItemBuilderImpl transformMeta(Function<ItemMeta, ItemMeta> function) {
        ItemMeta meta = itemStack.getItemMeta();
        ConditionUtils.notNull(meta, "ItemMeta is null!");

        ItemMeta result = null;
        try {
            result = function.apply(meta);
        } finally {
            if (result != null)
                itemStack.setItemMeta(result);
        }
        return this;
    }

    @Override
    public <T extends ItemMeta> ItemBuilderImpl transformMeta(Class<T> metaClass, Function<T, T> function) {
        ItemMeta meta = itemStack.getItemMeta();
        ConditionUtils.notNull(meta, "ItemMeta is null!");
        ConditionUtils.is(metaClass.isInstance(meta), "ItemMeta is not instance of " + metaClass.getName());

        T result = null;
        try {
            result = function.apply(metaClass.cast(meta));
        } finally {
            if (result != null)
                itemStack.setItemMeta(result);
        }
        return this;
    }

    @Override
    public ItemBuilderImpl transformItemStack(Function<ItemStack, ItemStack> function) {
        this.itemStack = function.apply(this.itemStack);
        return this;
    }

    @Override
    public ItemBuilderImpl clone() {
        return new ItemBuilderImpl(this.itemStack.clone());
    }

    @Override
    public ItemStack build() {
        return itemStack;
    }

    @Override
    public Material getType() {
        return this.itemStack.getType();
    }

}
