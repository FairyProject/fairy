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

package org.fairy.bukkit.menu.pagination;

import org.fairy.bukkit.menu.Button;

import java.util.List;

public abstract class PaginatedListMenu extends PaginatedMenu {

    private Button[] buttons;

    @Override
    public final int getMaxPages() {
        return (int) Math.ceil(buttons.length / (double) this.getMaxSizePerPage());
    }

    @Override
    protected final void drawPage(boolean firstInitial, int page) {
        this.clear();

        if (!this.isOnlyFirstInitial() || firstInitial) {
            this.buttons = this.getButtons().toArray(new Button[0]);
        }

        for (int i = 0; i < this.getMaxSizePerPage(); i++) {
            int index = (page - 1) * this.getMaxSizePerPage() + i;
            if (index >= this.buttons.length) {
                break;
            }

            this.set(i, this.buttons[index]);
        }
    }

    @Override
    public int getSize() {
        return this.isFixedInventorySize() ? this.getMaxSizePerPage() + this.getGlobalSize() : -1;
    }

    public abstract List<Button> getButtons();

    public boolean isOnlyFirstInitial() {
        return false;
    }

    public boolean isFixedInventorySize() {
        return false;
    }
}
