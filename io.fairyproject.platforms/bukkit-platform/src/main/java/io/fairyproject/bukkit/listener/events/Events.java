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

package io.fairyproject.bukkit.listener.events;

import io.fairyproject.Fairy;
import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.bukkit.listener.ListenerSubscription;
import io.fairyproject.bukkit.metadata.Metadata;
import io.fairyproject.bukkit.util.JavaPluginUtil;
import io.fairyproject.log.Log;
import io.fairyproject.metadata.MetadataKey;
import io.fairyproject.util.terminable.TerminableConsumer;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Predicate;

@UtilityClass
public class Events {

    public final MetadataKey<EventSubscriptionList> SUBSCRIPTION_LIST = MetadataKey.create(Fairy.METADATA_PREFIX + "SubscriptionList", EventSubscriptionList.class);

    public final Consumer<Cancellable> CANCEL = e -> e.setCancelled(true);
    public final Predicate<Cancellable> IGNORE_CANCELLED = e -> !e.isCancelled();
    public final Predicate<Cancellable> IGNORE_UNCANCELLED = Cancellable::isCancelled;
    public final Predicate<PlayerLoginEvent> IGNORE_DISALLOWED_LOGIN = e -> e.getResult() == PlayerLoginEvent.Result.ALLOWED;
    public final Predicate<AsyncPlayerPreLoginEvent> IGNORE_DISALLOWED_PRE_LOGIN = e -> e.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED;

    public final Predicate<PlayerMoveEvent> IGNORE_SAME_BLOCK = e ->
            e.getFrom().getBlockX() != e.getTo().getBlockX() ||
                    e.getFrom().getBlockZ() != e.getTo().getBlockZ() ||
                    e.getFrom().getBlockY() != e.getTo().getBlockY() ||
                    !e.getFrom().getWorld().equals(e.getTo().getWorld());

    public final Predicate<PlayerMoveEvent> IGNORE_SAME_BLOCK_AND_Y = e ->
            e.getFrom().getBlockX() != e.getTo().getBlockX() ||
                    e.getFrom().getBlockZ() != e.getTo().getBlockZ() ||
                    !e.getFrom().getWorld().equals(e.getTo().getWorld());

    public final Predicate<PlayerMoveEvent> IGNORE_SAME_CHUNK = e ->
            (e.getFrom().getBlockX() >> 4) != (e.getTo().getBlockX() >> 4) ||
                    (e.getFrom().getBlockZ() >> 4) != (e.getTo().getBlockZ() >> 4) ||
                    !e.getFrom().getWorld().equals(e.getTo().getWorld());

    public ListenerSubscription subscribe(Listener... listeners) {
        if (listeners.length == 0) {
            return null;
        }

        Listener mainListener = listeners[0];
        Plugin plugin = JavaPluginUtil.getProvidingPlugin(mainListener.getClass());

        if (plugin == null) {
            plugin = FairyBukkitPlatform.PLUGIN;
        }
        if (!plugin.isEnabled()) {
            Log.error("The plugin hasn't enabled but trying to register listener " + mainListener.getClass().getSimpleName());
        }

        TerminableConsumer terminable = FairyBukkitPlatform.INSTANCE;
        if (plugin instanceof TerminableConsumer) {
            terminable = (TerminableConsumer) plugin;
        }

        final ListenerSubscription listenerSubscription = new ListenerSubscription(listeners, plugin, terminable);
        listenerSubscription.register();

        return listenerSubscription;
    }

    public <T extends Event> T call(T t) {
        Bukkit.getPluginManager().callEvent(t);
        return t;
    }

    public <T extends Event> EventSubscribeBuilder<T> subscribe(Class<T> type) {
        return new EventSubscribeBuilder<>(type);
    }

    public Predicate<? extends PlayerEvent> onlyForPlayer(Player player) {
        return (Predicate<PlayerEvent>) playerEvent -> playerEvent.getPlayer() == player;
    }

    @Nullable
    public EventSubscription<?> getSubscription(Player player, String metadata) {
        return Events.getSubscriptionList(player).get(metadata);
    }

    public EventSubscriptionList getSubscriptionList(Player player) {
        return Metadata.provideForPlayer(player).getOrPut(SUBSCRIPTION_LIST, EventSubscriptionList::new);
    }

    public void unregisterAll(Player player) {

        Events.getSubscriptionList(player).clear();

    }
}
