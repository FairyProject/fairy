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

package io.fairyproject.bukkit.util.items.behaviour;

import io.fairyproject.bukkit.listener.ListenerRegistry;
import io.fairyproject.bukkit.util.items.FairyItem;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

@Getter
public abstract class ItemBehaviour {

    public static ItemBehaviour interact(ListenerRegistry listenerRegistry, ItemBehaviourInteract.Callback callback, Action... allowedActions) {
        return new ItemBehaviourInteract(listenerRegistry, callback, allowedActions);
    }

    public static ItemBehaviour blockPlace(ListenerRegistry listenerRegistry, ItemBehaviourPlace.Callback callback) {
        return new ItemBehaviourPlace(listenerRegistry, callback);
    }

    public static <E extends Event> ItemBehaviour ofEvent(Class<E> classToRegister, BiConsumer<E, ItemBehaviourEvent<E>> listener) {
        AtomicReference<ItemBehaviourEvent<E>> reference = new AtomicReference<>();
        reference.set(new ItemBehaviourEvent<E>(classToRegister) {
            @Override
            public void call(E event) {
                listener.accept(event, reference.get());
            }
        });
        return reference.get();
    }

    protected FairyItem item;
    private final List<BiPredicate<Player, ItemStack>> filters;

    public ItemBehaviour() {
        this.filters = new ArrayList<>(1);
        if (this.shouldFilterItemKey()) {
            this.filter((player, itemStack) -> this.item.isSimilar(itemStack));
        }
    }

    public boolean shouldFilterItemKey() {
        return true;
    }

    public final void init(FairyItem item) {
        this.item = item;
        this.onInit(item);
    }

    protected void onInit(FairyItem item) {
        // to be overridden
    }

    public void unregister() {
        // to be overridden
    }

    public ItemBehaviour filter(BiPredicate<Player, ItemStack> filter) {
        this.filters.add(filter);
        return this;
    }

    public boolean matches(Player player, ItemStack itemStack) {
        for (BiPredicate<Player, ItemStack> filter : this.filters) {
            if (!filter.test(player, itemStack)) {
                return false;
            }
        }

        return true;
    }

}
