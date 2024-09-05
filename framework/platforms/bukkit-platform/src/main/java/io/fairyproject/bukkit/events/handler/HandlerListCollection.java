package io.fairyproject.bukkit.events.handler;

import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.bukkit.events.GlobalEventListener;
import org.bukkit.event.*;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

public class HandlerListCollection extends ArrayList<HandlerList> implements Listener, EventExecutor {

    private static final boolean HAS_HANDSHAKE_EVENT;

    private final ArrayList<HandlerList> forward;
    private final GlobalEventListener globalEventListener;

    static {
        boolean hasHandshakeEvent = false;
        try {
            Class.forName("com.destroystokyo.paper.event.player.PlayerHandshakeEvent");
            hasHandshakeEvent = true;
        } catch (ClassNotFoundException ignored) {
        }
        HAS_HANDSHAKE_EVENT = hasHandshakeEvent;
    }

    public HandlerListCollection(ArrayList<HandlerList> forward, GlobalEventListener globalEventListener) {
        this.forward = forward;
        this.globalEventListener = globalEventListener;

        for (HandlerList handlerList : this.forward) {
            registerHandlerList(handlerList);
        }
    }

    @Override
    public boolean add(HandlerList handlerList) {
        boolean result = forward.add(handlerList);
        if (result) {
            registerHandlerList(handlerList);
        }

        return result;
    }

    private void registerHandlerList(HandlerList handlerList) {
        handlerList.register(new RegisteredListener(
                this,
                this,
                EventPriority.NORMAL,
                FairyBukkitPlatform.PLUGIN,
                false
        ));
    }

    @Override
    public boolean remove(Object o) {
        return forward.remove(o);
    }

    @Override
    public HandlerList remove(int index) {
        return forward.remove(index);
    }

    @Override
    public @NotNull Iterator<HandlerList> iterator() {
        return forward.iterator();
    }

    @Override
    public @NotNull ListIterator<HandlerList> listIterator() {
        return forward.listIterator();
    }

    @Override
    public @NotNull ListIterator<HandlerList> listIterator(int index) {
        return forward.listIterator(index);
    }

    @Override
    public void execute(@NotNull Listener listener, @NotNull Event event) {
        if (HAS_HANDSHAKE_EVENT) {
            if (event instanceof com.destroystokyo.paper.event.player.PlayerHandshakeEvent) {
                com.destroystokyo.paper.event.player.PlayerHandshakeEvent handshakeEvent = (com.destroystokyo.paper.event.player.PlayerHandshakeEvent) event;
                HandlerList handlers = handshakeEvent.getHandlers();
                if (handlers.getRegisteredListeners().length == 1) {
                    handshakeEvent.setCancelled(true);
                    handlers.unregister(this);
                    return;
                }
            }
        }

        globalEventListener.onEventFired(event);
    }
}
