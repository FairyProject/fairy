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

package io.fairyproject.bukkit.menu.node;

import io.fairyproject.bukkit.menu.Menu;
import io.fairyproject.bukkit.menu.node.condition.Condition;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;

/**
 * @since 0.6.6b3
 */
public interface MenuNode {

    static MenuNode of(Menu menu) {
        return new MenuNodeImpl(menu);
    }

    Menu getMenu();

    void setParent(MenuNode node);

    /**
     * set a children node where when condition meet, it will open the target menu node.
     *
     * @param condition the condition requirement
     * @param node the target menu node
     * @return the target menu node
     */
    @Contract("_, _ -> param2")
    MenuNode setChild(Condition condition, MenuNode node);

    /**
     * set a children node where when condition meet, it will open the target menu.
     *
     * @param condition the condition requirement
     * @param menu the target menu
     * @return the target menu node
     */
    default MenuNode setChild(Condition condition, Menu menu) {
        return this.setChild(condition, MenuNode.of(menu));
    }

    /**
     * sets a condition where when condition meet, it will open the parent menu.
     *
     * @param condition the condition requirement
     * @return this
     */
    @Contract("_ -> this")
    MenuNode setParentCondition(Condition condition);

    void openPrevious(Player player);

    void openNext(Player player, Condition condition);

    /**
     * Opens the menu and starts the node chain
     *
     * @param player the player
     */
    void open(Player player);

}
