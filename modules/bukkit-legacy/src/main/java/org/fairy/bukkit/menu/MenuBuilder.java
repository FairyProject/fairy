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

package org.fairy.bukkit.menu;

import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MenuBuilder {

    public static MenuBuilder builder() {
        return new MenuBuilder();
    }

    private Function<Player, String> title;
    private Function<Player, Menu> onOpen, onClose;
    private DrawCallback drawCallback;
    private int size;

    private final Map<Integer, Button> buttons;

    public MenuBuilder() {
        this.buttons = new HashMap<>();
        this.size = -1;
    }

    public MenuBuilder title(String title) {
        this.title = player -> title;
        return this;
    }

    public MenuBuilder title(Function<Player, String> title) {
        this.title = title;
        return this;
    }

    public MenuBuilder set(int slot, Button button) {
        this.buttons.put(slot, button);
        return this;
    }

    public MenuBuilder set(int x, int y, Button button) {
        return this.set(Menu.getSlot(x, y), button);
    }

    public MenuBuilder clearButtons() {
        this.buttons.clear();
        return this;
    }

    public MenuBuilder onDraw(DrawCallback drawCallback) {
        this.drawCallback = drawCallback;
        return this;
    }

    public MenuBuilder onOpen(Function<Player, Menu> function) {
        this.onOpen = function;
        return this;
    }

    public MenuBuilder onClose(Function<Player, Menu> function) {
        this.onClose = function;
        return this;
    }

    public MenuBuilder size(int size) {
        this.size = size;
        return this;
    }

    public Menu build() {
        Preconditions.checkNotNull(this.title, "title == null");
        return new Menu() {
            @Override
            public void draw(boolean firstInitial) {
                if (firstInitial) {
                    for (Map.Entry<Integer, Button> entry : buttons.entrySet()) {
                        this.set(entry.getKey(), entry.getValue());
                    }
                }

                if (drawCallback != null) {
                    drawCallback.draw(this, firstInitial);
                }
            }

            @Override
            public String getTitle() {
                return title.apply(player);
            }

            @Override
            public void onOpen(Player player) {
                if (onOpen != null) {
                    onOpen.apply(player);
                }
            }

            @Override
            public void onClose(Player player) {
                if (onClose != null) {
                    onClose.apply(player);
                }
            }

            @Override
            public int getSize() {
                return size;
            }
        };
    }

    public Menu open(Player player) {
        Menu menu = this.build();

        menu.open(player);
        return menu;
    }

    public interface DrawCallback {

        void draw(Menu menu, boolean firstInitial);

    }

}
