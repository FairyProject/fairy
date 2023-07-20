/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
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

package io.fairyproject.bukkit.menu.sequence;

import io.fairyproject.bukkit.menu.Menu;
import io.fairyproject.bukkit.menu.sequence.condition.Condition;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class MenuNodeImpl implements MenuNode {

    private final Menu menu;
    private final Map<Condition, MenuNode> children;
    @Nullable
    private MenuNode parent;

    public MenuNodeImpl(Menu menu) {
        this.menu = menu;
        this.children = new HashMap<>();
    }

    @Override
    public void setParent(@Nullable MenuNode node) {
        this.parent = node;
    }

    public MenuNode setChild(Condition condition, MenuNode child) {
        this.children.put(condition, child);
        return child;
    }

    public void next(Player player, Condition condition) {
        MenuNode child = this.children.getOrDefault(condition, null);

        if (child != null) {
            child.open(player);
        }
    }

    @Override
    public void open(Player player) {
        for (Condition condition : children.keySet()) {
            condition.setup(this);
        }
        this.menu.open(player);
    }

}
