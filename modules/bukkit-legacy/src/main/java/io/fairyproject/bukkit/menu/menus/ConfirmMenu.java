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

package io.fairyproject.bukkit.menu.menus;

import io.fairyproject.bukkit.menu.Menu;
import io.fairyproject.bukkit.menu.Button;
import io.fairyproject.bukkit.menu.buttons.ConfirmationButton;
import io.fairyproject.bukkit.util.TypeCallback;

public class ConfirmMenu extends Menu {

	private final String title;
	private final TypeCallback<Boolean> response;
	private final boolean closeAfterResponse;
	private final Button[] centerButtons;

	public ConfirmMenu(final String title, final TypeCallback<Boolean> response, final boolean closeAfter, final Button... centerButtons) {
		this.title = title;
		this.response = response;
		this.closeAfterResponse = closeAfter;
		this.centerButtons = centerButtons;
	}

	@Override
	public void draw(boolean firstInitial) {
		if (!firstInitial) {
			return;
		}

		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				this.set(x, y, new ConfirmationButton(true, response, closeAfterResponse));
				this.set(8 - x, y, new ConfirmationButton(false, response, closeAfterResponse));
			}
		}

		if (centerButtons != null) {
			for (int i = 0; i < centerButtons.length; i++) {
				if (centerButtons[i] != null) {
					this.set(getSlot(4, i), centerButtons[i]);
				}
			}
		}
	}

	@Override
	public String getTitle() {
		return title;
	}

}
