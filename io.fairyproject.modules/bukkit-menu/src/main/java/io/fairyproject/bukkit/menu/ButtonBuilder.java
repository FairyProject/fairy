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

package io.fairyproject.bukkit.menu;

import io.fairyproject.bukkit.util.items.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ButtonBuilder {

    public static ButtonBuilder builder() {
        return new ButtonBuilder();
    }

    public static ButtonBuilder of(ItemStack itemStack) {
        return new ButtonBuilder(itemStack);
    }

    private Function<Player, ItemStack> itemStackFunction;
    private final List<CancelConsumer> cancelConsumers;
    private final List<Callback> callbacks;

    public ButtonBuilder() {
        this.cancelConsumers = new ArrayList<>();
        this.callbacks = new ArrayList<>();
    }

    public ButtonBuilder(ItemStack itemStack) {
        this();

        this.itemStackFunction = player -> itemStack;
    }

    public ButtonBuilder(ItemBuilder itemBuilder) {
        this(itemBuilder.build());
    }

    public ButtonBuilder item(ItemBuilder itemBuilder) {
        return this.item(itemBuilder.build());
    }

    public ButtonBuilder item(ItemStack itemStack) {
        return this.item(player -> itemStack);
    }

    public ButtonBuilder item(Function<Player, ItemStack> function) {
        this.itemStackFunction = function;
        return this;
    }

    public ButtonBuilder callback(Callback callback) {
        this.callbacks.add(callback);
        return this;
    }

    public ButtonBuilder cancel() {
        this.cancelConsumers.add((player, slot, clickType) -> true);
        return this;
    }

    public ButtonBuilder noCancel() {
        this.cancelConsumers.add((player, slot, clickType) -> false);
        return this;
    }

    public ButtonBuilder shouldCancel(CancelConsumer function) {
        this.cancelConsumers.add(function);
        return this;
    }

    public ButtonBuilder cleanup() {
        this.itemStackFunction = null;
        this.cancelConsumers.clear();
        this.callbacks.clear();
        return this;
    }

    public Button build() {
        if (this.itemStackFunction == null) {
            throw new IllegalArgumentException("No Item Registered");
        }

        return new Button() {
            @Override
            public ItemStack getButtonItem(Player player) {
                return itemStackFunction.apply(player);
            }

            @Override
            public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
                for (Callback callback : callbacks) {
                    callback.click(player, slot, clickType, hotbarButton);
                }
            }

            @Override
            public boolean shouldCancel(Player player, int slot, ClickType clickType) {
                for (CancelConsumer consumer : cancelConsumers) {
                    if (!consumer.shouldCancel(player, slot, clickType)) {
                        return false;
                    }
                }

                return true;
            }
        };
    }

    public interface Callback {

        void click(Player player, int slot, ClickType clickType, int hotbarButton);

    }

    public interface CancelConsumer {

        boolean shouldCancel(Player player, int slot, ClickType clickType);

    }

}
