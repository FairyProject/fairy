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

import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.bukkit.listener.events.EventSubscription;
import io.fairyproject.bukkit.listener.events.Events;
import io.fairyproject.bukkit.util.items.FairyItem;
import lombok.Getter;
import org.bukkit.event.Event;

@Getter
public abstract class ItemBehaviourEvent<E extends Event> extends ItemBehaviour {

    private final Class<E> classToRegister;
    private EventSubscription<E> subscription;

    public ItemBehaviourEvent(Class<E> classToRegister) {
        this.classToRegister = classToRegister;
    }

    @Override
    public void onInit(FairyItem item) {
        this.subscription = Events.subscribe(classToRegister)
                .listen((subscription, e) -> this.call(e))
                .build(FairyBukkitPlatform.PLUGIN);
    }

    @Override
    public void unregister() {
        super.unregister();
        if (this.subscription != null) {
            this.subscription.unregister();
        }
    }

    public abstract void call(E event);

}
