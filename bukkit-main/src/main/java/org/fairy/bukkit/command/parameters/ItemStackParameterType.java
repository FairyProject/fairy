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

package org.fairy.bukkit.command.parameters;

import com.google.common.collect.ImmutableList;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.fairy.bukkit.command.util.ItemUtil;
import org.fairy.bean.Component;

import java.util.List;
import java.util.Set;

@Component
public class ItemStackParameterType extends BukkitParameterHolder<ItemStack> {

	@Override
	public ItemStack transform(final CommandSender sender, final String source) {
		final ItemStack item = ItemUtil.get(source);

		if (item == null) {
			sender.sendMessage(ChatColor.RED + "No item with the name " + source + " found.");
			return null;
		}

		return item;
	}

	@Override
	public List<String> tabComplete(final Player sender, final Set<String> flags, final String source) {
		return ImmutableList.of(); // it would probably be too intensive to go through all the aliases
	}

	@Override
	public Class[] type() {
		return new Class[] {ItemStack.class};
	}
}
