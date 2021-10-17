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

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import io.fairyproject.bukkit.Imanity;
import io.fairyproject.bukkit.util.nms.NBTEditor;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder implements Listener, Cloneable {

	private ItemStack itemStack;

	public ItemBuilder(final Material mat) {
		itemStack = new ItemStack(mat);
	}

	public ItemBuilder(final ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	public ItemBuilder amount(final int amount) {
		itemStack.setAmount(amount);
		return this;
	}

	public ItemBuilder name(final String name) {
		final ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		itemStack.setItemMeta(meta);
		return this;
	}

	public ItemBuilder lore(final Iterable<String> lore) {
		final ItemMeta meta = itemStack.getItemMeta();

		List<String> toSet = meta.getLore();
		if (toSet == null) {
			toSet = new ArrayList<>();
		}

		for (final String string : lore) {
			toSet.add(ChatColor.translateAlternateColorCodes('&', string));
		}

		meta.setLore(toSet);
		itemStack.setItemMeta(meta);
		return this;
	}

	public ItemBuilder lore(final String... lore) {
		final ItemMeta meta = itemStack.getItemMeta();

		List<String> toSet = meta.getLore();
		if (toSet == null) {
			toSet = new ArrayList<>();
		}

		for (final String string : lore) {
			toSet.add(ChatColor.translateAlternateColorCodes('&', string));
		}

		meta.setLore(toSet);
		itemStack.setItemMeta(meta);
		return this;
	}

	public ItemBuilder durability(final int durability) {
		itemStack.setDurability((short) - (durability - itemStack.getType().getMaxDurability()));
		return this;
	}

	public ItemBuilder data(final int data) {
		itemStack.setDurability((short) data);
		return this;
	}

	public ItemBuilder enchantment(final Enchantment enchantment, final int level) {
		itemStack.addUnsafeEnchantment(enchantment, level);
		return this;
	}

	public ItemBuilder enchantment(final Enchantment enchantment) {
		itemStack.addUnsafeEnchantment(enchantment, 1);
		return this;
	}

	public ItemBuilder type(final Material material) {
		itemStack.setType(material);
		return this;
	}

	public ItemBuilder clearLore() {
		final ItemMeta meta = itemStack.getItemMeta();
		meta.setLore(new ArrayList<String>());
		itemStack.setItemMeta(meta);
		return this;
	}

	public ItemBuilder clearEnchantments() {
		for (final Enchantment e : itemStack.getEnchantments().keySet()) {
			itemStack.removeEnchantment(e);
		}
		return this;
	}

	public ItemBuilder color(final Color color) {
		if (itemStack.getType() == Material.LEATHER_BOOTS || itemStack.getType() == Material.LEATHER_CHESTPLATE || itemStack.getType() == Material.LEATHER_HELMET
				|| itemStack.getType() == Material.LEATHER_LEGGINGS) {
			final LeatherArmorMeta meta = (LeatherArmorMeta) itemStack.getItemMeta();
			meta.setColor(color);
			itemStack.setItemMeta(meta);
			return this;
		} else
			throw new IllegalArgumentException("color() only applicable for leather armor!");
	}

	public ItemBuilder skull(String owner) {
		if (itemStack.getType() == Material.SKULL_ITEM && itemStack.getDurability() == (byte) 3) {
			SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
			skullMeta.setOwner(owner);
			itemStack.setItemMeta(skullMeta);
			return this;
		} else {
			throw new IllegalArgumentException("skull() only applicable for human skull item!");
		}
	}

	public ItemBuilder skull(Player owner) {
		if (itemStack.getType() == Material.SKULL_ITEM && itemStack.getDurability() == (byte) 3) {
			SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
			Imanity.IMPLEMENTATION.setSkullGamwProfile(skullMeta, owner);
			itemStack.setItemMeta(skullMeta);
			return this;
		} else {
			throw new IllegalArgumentException("skull() only applicable for human skull item!");
		}
	}

	public ItemBuilder shiny() {
		ItemMeta meta = this.itemStack.getItemMeta();

		meta.addEnchant(Enchantment.PROTECTION_FIRE, 1, false);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		return this;
	}

	public ItemBuilder tag(Object value, String... key) {
		this.itemStack = NBTEditor.set(this.itemStack, value, key);
		return this;
	}

	public ItemBuilder itemFlag(ItemFlag itemFlag) {
		ItemMeta im = itemStack.getItemMeta();
		im.addItemFlags(itemFlag);
		itemStack.setItemMeta(im);
		return this;
	}

	public ItemBuilder removeItemFlag(ItemFlag itemFlag) {
		ItemMeta im = itemStack.getItemMeta();
		if (im.hasItemFlag(itemFlag)) {
			im.removeItemFlags(itemFlag);
		}
		itemStack.setItemMeta(im);
		return this;
	}

	@Override
	public ItemBuilder clone() {
		return new ItemBuilder(this.itemStack.clone());
	}

	public ItemStack build() {
		return itemStack;
	}

	public Material getType() {
		return this.itemStack.getType();
	}

}
