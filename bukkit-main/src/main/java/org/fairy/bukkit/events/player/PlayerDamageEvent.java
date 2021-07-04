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

package org.fairy.bukkit.events.player;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerEvent;

public class PlayerDamageEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlerlist = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlerlist;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerlist;
    }

    @Getter
    private EntityDamageEvent entityDamageEvent;

    public PlayerDamageEvent(Player player, EntityDamageEvent entityDamageEvent) {
        super(player);
        this.entityDamageEvent = entityDamageEvent;
    }

    @Override
    public void setCancelled(boolean b) {
        this.entityDamageEvent.setCancelled(b);
    }

    @Override
    public boolean isCancelled() {
        return this.entityDamageEvent.isCancelled();
    }

    public void setDamage(double damage) {
        this.entityDamageEvent.setDamage(damage);
    }

    public double getDamage() {
        return this.entityDamageEvent.getDamage();
    }

    public double getFinalDamage() {
        return this.entityDamageEvent.getFinalDamage();
    }

    public EntityDamageEvent.DamageCause getCause() {
        return this.entityDamageEvent.getCause();
    }
}
