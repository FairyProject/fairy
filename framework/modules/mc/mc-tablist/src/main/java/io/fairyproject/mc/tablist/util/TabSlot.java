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

package io.fairyproject.mc.tablist.util;

import io.fairyproject.mc.tablist.TabColumn;
import net.kyori.adventure.text.Component;
import lombok.Getter;

@Getter
public class TabSlot {
    private TabColumn column;
    private int ping;
    private int slot;
    private Component text;
    private Skin skin;

    public TabSlot() {
        this.column = TabColumn.LEFT;
        this.slot = 1;
        this.text = Component.empty();
        this.skin = Skin.GRAY;
    }

    public TabSlot text(Component text) {
        this.text = text;
        return this;
    }

    public TabSlot skin(Skin skin) {
        this.skin = skin;
        return this;
    }

    public TabSlot slot(int slot) {
        this.slot = slot;
        return this;
    }

    public TabSlot ping(int ping) {
        this.ping = ping;
        return this;
    }

    public TabSlot column(TabColumn tabColumn) {
        this.column = tabColumn;
        return this;
    }
}
