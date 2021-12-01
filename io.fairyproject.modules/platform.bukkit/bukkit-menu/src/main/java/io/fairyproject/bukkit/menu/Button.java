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

package io.fairyproject.bukkit.menu;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import io.fairyproject.bukkit.util.items.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public abstract class Button {

	public static Button placeholder(final XMaterial material, String title) {
		return ButtonBuilder.of(new ItemBuilder(material.parseItem())
				.name(title)
				.build()
		).cancel().build();
	}

	public static void playFail(Player player) {
		player.playSound(player.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.parseSound(), 20F, 0.1F);
	}

	public static void playSuccess(Player player) {
		player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_HARP.parseSound(), 20F, 15F);
	}

	public static void playNeutral(Player player) {
		player.playSound(player.getLocation(), XSound.UI_BUTTON_CLICK.parseSound(), 20F, 1F);
	}

	/**
	 * Get Display ItemStack
	 *
	 * @param player The Player to display on
	 * @return The Display ItemStack
	 */
	public abstract ItemStack getButtonItem(Player player);

	/**
	 * This method will be called when player clicked this button
	 *
	 * @param player The Player Who Clicked
	 * @param slot Which Slot the player Clicked
	 * @param clickType How the player clicked it
	 * @param hotbarButton The hotbar key
	 */
	public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
	}

	public boolean shouldCancel(Player player, int slot, ClickType clickType) {
		return (true);
	}

	public boolean shouldUpdate(Player player, int slot, ClickType clickType) {
		return (false);
	}

}
