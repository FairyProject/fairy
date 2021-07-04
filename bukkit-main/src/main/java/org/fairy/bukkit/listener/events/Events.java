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

package org.fairy.bukkit.listener.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.fairy.Fairy;
import org.fairy.bukkit.metadata.Metadata;
import org.fairy.metadata.MetadataKey;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class Events {

    public static final MetadataKey<EventSubscriptionList> SUBSCRIPTION_LIST = MetadataKey.create(Fairy.METADATA_PREFIX + "SubscriptionList", EventSubscriptionList.class);

    public static final Predicate<Cancellable> IGNORE_CANCELLED = e -> !e.isCancelled();
    public static final Predicate<Cancellable> IGNORE_UNCANCELLED = Cancellable::isCancelled;
    public static final Predicate<PlayerLoginEvent> IGNORE_DISALLOWED_LOGIN = e -> e.getResult() == PlayerLoginEvent.Result.ALLOWED;
    public static final Predicate<AsyncPlayerPreLoginEvent> IGNORE_DISALLOWED_PRE_LOGIN = e -> e.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED;

    public static final Predicate<PlayerMoveEvent> IGNORE_SAME_BLOCK = e ->
            e.getFrom().getBlockX() != e.getTo().getBlockX() ||
                    e.getFrom().getBlockZ() != e.getTo().getBlockZ() ||
                    e.getFrom().getBlockY() != e.getTo().getBlockY() ||
                    !e.getFrom().getWorld().equals(e.getTo().getWorld());

    public static final Predicate<PlayerMoveEvent> IGNORE_SAME_BLOCK_AND_Y = e ->
            e.getFrom().getBlockX() != e.getTo().getBlockX() ||
                    e.getFrom().getBlockZ() != e.getTo().getBlockZ() ||
                    !e.getFrom().getWorld().equals(e.getTo().getWorld());

    public static final Predicate<PlayerMoveEvent> IGNORE_SAME_CHUNK = e ->
            (e.getFrom().getBlockX() >> 4) != (e.getTo().getBlockX() >> 4) ||
                    (e.getFrom().getBlockZ() >> 4) != (e.getTo().getBlockZ() >> 4) ||
                    !e.getFrom().getWorld().equals(e.getTo().getWorld());

    public static <T extends Event> EventSubscribeBuilder<T> subscribe(Class<T> type) {
        return new EventSubscribeBuilder<>(type);
    }

    public static Predicate<? extends PlayerEvent> onlyForPlayer(Player player) {
        return (Predicate<PlayerEvent>) playerEvent -> playerEvent.getPlayer() == player;
    }

    @Nullable
    public static EventSubscription<?> getSubscription(Player player, String metadata) {
        return Events.getSubscriptionList(player).get(metadata);
    }

    public static EventSubscriptionList getSubscriptionList(Player player) {
        return Metadata.provideForPlayer(player).getOrPut(SUBSCRIPTION_LIST, EventSubscriptionList::new);
    }

    public static void unregisterAll(Player player) {

        Events.getSubscriptionList(player).clear();

    }
}
