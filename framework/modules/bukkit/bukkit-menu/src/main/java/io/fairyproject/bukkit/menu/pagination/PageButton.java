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

package io.fairyproject.bukkit.menu.pagination;

import lombok.AllArgsConstructor;
import lombok.Getter;
import io.fairyproject.bukkit.menu.Button;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
@Getter
public class PageButton extends Button {

	private final Action action;
	private final PaginatedMenu menu;

	@Override
	public ItemStack getButtonItem(final Player player) {
		return this.menu.getPageButtonItem(player, this);
	}

	@Override
	public void clicked(final Player player, final int i, final ClickType clickType, final int hb) {
		if (action == Action.VIEW_ALL_PAGES || clickType == ClickType.RIGHT) {
			new ViewAllPagesMenu(this.menu).open(player);
			playNeutral(player);
		} else {
			if (this.hasNext()) {
				this.menu.modPage(player, this.getModByAction());
				Button.playNeutral(player);
			} else {
				Button.playFail(player);
			}
		}
	}

	public int getModByAction() {
		switch (this.action) {
			case GO_FORWARD:
				return 1;
			case GO_BACKWARD:
				return -1;
			default:
				return Integer.MIN_VALUE;
		}
	}

	public boolean hasNext() {
		final int pg = this.menu.getPage() + this.getModByAction();
		return pg > 0 && this.menu.getMaxPages() >= pg;
	}

	public enum Action {

		GO_FORWARD,
		GO_BACKWARD,
		VIEW_ALL_PAGES

	}

}
