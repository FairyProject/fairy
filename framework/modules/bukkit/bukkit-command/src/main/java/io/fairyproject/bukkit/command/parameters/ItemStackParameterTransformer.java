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

package io.fairyproject.bukkit.command.parameters;

import com.cryptomorin.xseries.XMaterial;
import io.fairyproject.bukkit.command.util.CommandUtil;
import io.fairyproject.container.object.Obj;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Obj
public class ItemStackParameterTransformer extends BukkitArgTransformer<ItemStack> {

	@Override
	public ItemStack transform(final CommandSender sender, final String source) {
		final ItemStack item = CommandUtil.get(source);

		if (item == null) {
			return this.fail("No item with the name " + source + " found.");
		}

		return item;
	}

	@Override
	public List<String> tabComplete(final Player sender, final String source) {
		if (!source.isEmpty()) {
			return Stream.of(XMaterial.values())
					.filter(XMaterial::isSupported)
					.map(XMaterial::name)
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	@Override
	public Class[] type() {
		return new Class[] {ItemStack.class};
	}
}
