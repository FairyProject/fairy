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

import io.fairyproject.bukkit.menu.Menu;
import io.fairyproject.bukkit.util.items.ItemBuilder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import io.fairyproject.bukkit.menu.Button;
import io.fairyproject.util.CC;

@Getter
public abstract class PaginatedMenu extends Menu {

    private DrawStatus status = DrawStatus.NONE;
    @Setter
    private int page = 1;

    @Override
    public final String getTitle() {
        return getPrePaginatedTitle() + " - " + page + "/" + getMaxPages();
    }

    @Override
    public final Menu clear() {
        switch (this.status) {
            case GLOBAL:
                return super.clearBetween(1, 7);
            case PER_PAGE:
                return super.clearBetween(9, 9 + this.getMaxSizePerPage());
            default:
                return super.clear();
        }
    }

    @Override
    protected final Menu set(int slot, Button button) {
        switch (this.status) {
            case PER_PAGE:
                return super.set(slot + 9, button);
            case GLOBAL:
            default:
                return super.set(slot, button);
        }
    }

    @Override
    public final int getSizeLimit() {
        switch (this.status) {
            case GLOBAL:
                return this.getGlobalSize();
            case PER_PAGE:
                return this.getMaxSizePerPage() + 9;
            default:
                return super.getSizeLimit();
        }
    }

    /**
     * Changes the page number
     *
     * @param mod    delta to modify the page number by
     */
    public final void modPage(Player player, int mod) {
        this.page += mod;

        this.clear();
        if (!this.isOpening()) {
            this.open(player);
        } else {
            this.render();
        }
    }

    public abstract int getMaxPages();

    @Override
    public final void draw(boolean firstInitial) {
        this.status = DrawStatus.GLOBAL;
        this.drawGlobal(firstInitial);

        this.status = DrawStatus.PER_PAGE;
        this.drawPage(firstInitial, this.page);

        this.status = DrawStatus.NONE;
    }

    protected void drawGlobal(boolean firstInitial) {
        if (firstInitial) {
            this.set(0, new PageButton(PageButton.Action.GO_BACKWARD, this));
            this.set(this.getGlobalSize() - 1, new PageButton(PageButton.Action.GO_FORWARD, this));
        }
    }

    protected abstract void drawPage(boolean firstInitial, int page);

    public int getGlobalSize() {
        return 9;
    }

    /**
     * @return The Max Slots per page, not including global
     */
    public int getMaxSizePerPage() {
        return 18;
    }

    /**
     * @return title of the inventory before the page number is added
     */
    public abstract String getPrePaginatedTitle();

    /**
     * @param player The Viewer
     * @param button The Button
     * @return The display ItemStack
     */
    public ItemStack getJumpToPageButtonItem(Player player, JumpToPageButton button) {
        return new ItemBuilder(button.isCurrent() ? Material.ENCHANTED_BOOK : Material.BOOK)
                .name("&ePage " + button.getPage())
                .lore(CC.SB_BAR, button.isCurrent() ? "&aThis is the current page" : "&fClick me jump to this page", CC.SB_BAR)
                .build();
    }

    /**
     * @param player The Viewer
     * @param button The Button
     * @return The display ItemStack
     */
    public ItemStack getPageButtonItem(Player player, PageButton button) {
        ItemBuilder itemBuilder = new ItemBuilder(Material.CARPET).lore(CC.SB_BAR);
        switch (button.getAction()) {
            case GO_BACKWARD:
                if (button.hasNext()) {
                    itemBuilder.name("&cPrevious Page")
                            .lore("&eLeft Click me jump to previous page");
                } else {
                    itemBuilder.name("&6You are currently at Last Page")
                            .lore("&cThere is no more page to go!");
                }
                itemBuilder.lore(" ", "&eRight Click to view all pages!", CC.SB_BAR);
                break;
            case GO_FORWARD:
                if (button.hasNext()) {
                    itemBuilder.name("&aNext Page")
                            .lore("&eLeft Click me jump to next page");
                } else {
                    itemBuilder.name("&6You are currently at First Page")
                            .lore("&cThere is no more page to go!");
                }
                itemBuilder.lore(" ", "&eRight Click to view all pages!", CC.SB_BAR);
                break;
            default:
            case VIEW_ALL_PAGES:
                itemBuilder.name("&eView All Pages")
                        .lore("&eClick me to view All Pages!", CC.SB_BAR);
                break;
        }

        return itemBuilder.build();
    }

    /**
     * @return The display title
     */
    public String getViewAllPagesMenuTitle() {
        return "&aAll Pages";
    }

    protected enum DrawStatus {

        NONE, GLOBAL, PER_PAGE

    }

}
