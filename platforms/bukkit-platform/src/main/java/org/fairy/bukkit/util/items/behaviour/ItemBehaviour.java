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

package org.fairy.bukkit.util.items.behaviour;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.fairy.bukkit.timer.PlayerCooldown;
import org.fairy.bukkit.util.items.ImanityItem;
import org.fairy.bukkit.util.text.IText;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

@Getter
public abstract class ItemBehaviour {

    public static ItemBehaviour interact(ItemBehaviourInteract.Callback callback, Action... allowedActions) {
        return new ItemBehaviourInteract(callback, allowedActions);
    }

    public static ItemBehaviour blockPlace(ItemBehaviourPlace.Callback callback) {
        return new ItemBehaviourPlace(callback);
    }

    public static ItemBehaviour cooldown(ItemBehaviour behaviour, long defaultCooldown, Plugin plugin) {
        return cooldown(behaviour, defaultCooldown, null, null, plugin);
    }

    public static ItemBehaviour cooldown(ItemBehaviour behaviour, long defaultCooldown, @Nullable IText cooldownMessage, @Nullable IText removeMessage, Plugin plugin) {
        PlayerCooldown cooldown = new PlayerCooldown(defaultCooldown, (player, cause) -> {
            if (removeMessage != null) {
                player.sendMessage(removeMessage.get(player));
            }
        }, plugin);
        return behaviour.filter((player, itemStack) -> {
            if (cooldown.isCooldown(player)) {
                if (cooldownMessage != null) {
                    player.sendMessage(cooldownMessage.get(player));
                }
                return false;
            }
            cooldown.addCooldown(player);
            return true;
        });
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

    protected ImanityItem item;
    private final List<BiPredicate<Player, ItemStack>> filters;

    public ItemBehaviour() {
        this.filters = new ArrayList<>(1);
        if (this.shouldFilterItemKey()) {
            this.filter((player, itemStack) -> {
                final String key = ImanityItem.getItemKeyFromBukkit(itemStack);
                return key != null && key.equals(this.item.getId());
            });
        }
    }

    public boolean shouldFilterItemKey() {
        return true;
    }

    public final void init0(ImanityItem item) {
        this.item = item;
        this.init(item);
    }

    public void unregister() {

    }

    public void init(ImanityItem item) {

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
