package io.fairyproject.mc.actionbar;

import io.fairyproject.container.*;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.metadata.PlayerOnlineValue;
import io.fairyproject.metadata.MetadataKey;
import io.fairyproject.task.Task;
import net.kyori.adventure.text.Component;
import io.fairyproject.util.terminable.Terminable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class ActionbarService {

    public static final MetadataKey<Component> ACTIONBAR_CURRENT = MetadataKey.create("fairy:actionbar", Component.class);

    private final List<ActionbarAdapter> adapters = new ArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final AtomicBoolean activated = new AtomicBoolean();

    @PreInitialize
    public void onPreInitialize() {
        ComponentRegistry.registerComponentHolder(ComponentHolder.builder()
                        .type(ActionbarAdapter.class)
                        .onEnable(obj -> this.registerAdapter((ActionbarAdapter) obj))
                        .onDisable(obj -> this.unregisterAdapter((ActionbarAdapter) obj))
                .build());
    }

    @PostInitialize
    public void onPostInitialize() {
        this.activate();
    }

    public void activate() {
        if (this.activated.compareAndSet(false, true)) {
            Containers.bindWith(this, Task.asyncRepeated(this::tick, this.getUpdateTick()));
        }
    }

    private void tick(Terminable terminable) {
        for (MCPlayer player : MCPlayer.all()) {
            Component current = player.metadata().getOrDefault(ACTIONBAR_CURRENT, Component.empty());
            Component component = this.buildActionbarComponent(player);
            if (component == null) {
                component = Component.empty();
            }

            if (current.equals(component) && current.equals(Component.empty())) {
                continue;
            }

            player.metadata().put(ACTIONBAR_CURRENT, PlayerOnlineValue.create(component, player));
            player.sendActionBar(component);
        }

        this.lock.readLock().lock();
        try {
            if (this.adapters.isEmpty()) {
                // No adapter registered at the moment
                terminable.closeAndReportException();
            }
        } finally {
            this.lock.readLock().unlock();
        }
    }

    @Nullable
    private Component buildActionbarComponent(MCPlayer player) {
        Component retVal = null;

        this.lock.readLock().lock();
        for (ActionbarAdapter adapter : this.getSortedAdapters()) {
            retVal = adapter.build(player);
            if (retVal != null && !retVal.equals(Component.empty())) {
                break;
            }
        }

        return retVal;
    }

    private int getUpdateTick() {
        int tick = 2;
        for (ActionbarAdapter adapter : this.getSortedAdapters()) {
            int adapterTick = adapter.ticks();
            if (adapterTick != -1) {
                tick = adapterTick;
                break;
            }
        }

        return tick;
    }

    private List<ActionbarAdapter> getSortedAdapters() {
        List<ActionbarAdapter> actionbarAdapters;

        this.lock.readLock().lock();
        try {
            actionbarAdapters = new ArrayList<>(this.adapters);
        } finally {
            this.lock.readLock().unlock();
        }

        actionbarAdapters.sort(Collections.reverseOrder(Comparator.comparingInt(ActionbarAdapter::priority)));
        return actionbarAdapters;
    }

    public void registerAdapter(ActionbarAdapter actionbarAdapter) {
        this.lock.writeLock().lock();
        try {
            this.adapters.add(actionbarAdapter);
        } finally {
            this.lock.writeLock().unlock();
        }

        this.activate();
    }

    public void unregisterAdapter(ActionbarAdapter actionbarAdapter) {
        this.lock.writeLock().lock();
        try {
            this.adapters.remove(actionbarAdapter);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

}
