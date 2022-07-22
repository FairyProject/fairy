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

package io.fairyproject.bukkit.storage;

import com.google.common.collect.Lists;
import io.fairyproject.StorageService;
import io.fairyproject.bukkit.util.JavaPluginUtil;
import io.fairyproject.container.*;
import io.fairyproject.log.Log;
import io.fairyproject.util.AsyncUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import io.fairyproject.storage.DataClosable;
import io.fairyproject.storage.PlayerStorage;
import io.fairyproject.bukkit.listener.events.Events;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.task.Task;
import org.checkerframework.checker.units.qual.C;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Threaded Player Storage
 * It will use Minecraft authentication thread pool for data loading
 * And bukkit async scheduler thread pool for data saving
 *
 * The reason we used Minecraft authentication thread pool for data loading is because
 * We want to ensure that player wasn't completely login before data were successfully loaded
 * And it will block authentication until data loaded, Which doesn't block main thread still
 *
 * To use it you need to create a class extends ThreadedPlayerStorage and made it a Service
 *
 * @param <T> the Data Class
 */
@ServiceDependency(StorageService.class)
public abstract class ThreadedPlayerStorage<T> implements PlayerStorage<T> {

    private final Object lock = new Object();

    private ThreadedPlayerStorageConfiguration<T> storageConfiguration;
    private Map<UUID, T> storedObjects;
    private Set<UUID> asyncLoginReject, syncLoginReject;

    @Autowired
    private ContainerContext containerContext;

    /**
     * build configuration for this storage, only called once on @PostInitialize
     *
     * @return configuration
     */
    protected abstract ThreadedPlayerStorageConfiguration<T> buildStorageConfiguration();

    /**
     * get current plugin
     *
     * @return plugin
     */
    protected Plugin getPlugin() {
        return JavaPluginUtil.getProvidingPlugin(this.getClass());
    }

    /**
     * should Player Storage print some information for debugging purposes
     *
     * @return is Debugging
     */
    public boolean isDebugging() {
        return false;
    }

    /**
     * on Data Successfully loaded in Minecraft authentication thread pool, will not block main thread
     * Warning: during this process it's still in Pre-Login, no Player Entity were registered
     *
     * @param uuid the UUID of the Player
     * @param t the Data
     */
    protected void onLoadedAsync(UUID uuid, String name, T t) {

    }

    /**
     * on Data Successfully loaded in Main Thread
     *
     * @param player the Player
     * @param t the Data
     */
    protected void onLoadedMain(Player player, T t) {

    }

    /**
     * on Data unloaded, in Main Thread, process will only run after CompletableFuture finished, will not block main during the process.
     *
     * @param player the Player
     * @param t the Data
     * @return CompletableFuture
     */
    protected CompletableFuture<T> onPreUnload(Player player, T t) {
        return CompletableFuture.completedFuture(t);
    }

    @Override
    public T find(UUID uuid) {
        return this.storedObjects.get(uuid);
    }

    @Override
    public DataClosable<T> findAndSave(UUID uuid) {
        return new DataClosable<>(this, uuid, this.find(uuid));
    }

    @Override
    public CompletableFuture<T> save(UUID uuid) {
        final T t = this.find(uuid);
        if (t == null) {
            return AsyncUtils.empty();
        }

        return this.save(uuid, t);
    }

    @Override
    public CompletableFuture<T> save(UUID uuid, T t) {
        return Task.supplyAsync(() -> {
            this.storageConfiguration.saveAsync(uuid, t);
            return t;
        });
    }

    @Override
    public void unload(UUID uuid) {
        final T t = this.find(uuid);
        if (t == null) {
            return;
        }

        Player player = Bukkit.getPlayer(uuid);
        this.onPreUnload(player, t)
                .thenRun(() -> this.storedObjects.remove(player.getUniqueId()));
    }

    @PostInitialize
    public final void onPostInitializeStorage() {
        final ContainerObj containerObj = ContainerRef.getObj(this.getClass());

        this.storedObjects = new ConcurrentHashMap<>();
        this.asyncLoginReject = new HashSet<>();
        this.syncLoginReject = new HashSet<>();

        this.storageConfiguration = this.buildStorageConfiguration();
        if (this.storageConfiguration == null) {
            Log.error("No storage configuration were enabled.");
            return;
        }

        Events.subscribe(AsyncPlayerPreLoginEvent.class)
                .priority(EventPriority.LOW)
                .listen(event -> {
                    // Process data in auth thread pool, this idea is quite genius to be honest.
                    final UUID uuid = event.getUniqueId();
                    final String name = event.getName();
                    if (this.isDebugging()) {
                        Log.info("Processing async pre-login for Player " + uuid + " - " + name);
                    }

                    if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
                        Log.info("Other plugin has cancelled pre-login event for " + uuid + " - " + name + ", Repository data for " + this.storageConfiguration.getName() + " will not be loaded.");
                        synchronized (this.lock) {
                            this.asyncLoginReject.add(uuid);
                        }
                        return;
                    }

                    try {
                        long time = System.currentTimeMillis();

                        // use computeIfAbsent so if it's already in cache, no load required
                        final T t = this.storedObjects.computeIfAbsent(uuid, ignored -> this.storageConfiguration.loadAsync(uuid, name));

                        final long diff = System.currentTimeMillis() - time;
                        if (diff > 1000L || this.isDebugging()) {
                            Log.warn("Server took " + diff + "ms to load data from repository " + this.storageConfiguration.getName() + " for " + uuid + " - " + name);
                        }

                        this.onLoadedAsync(uuid, name, t);
                    } catch (Exception exception) {
                        Log.error("Error occur while loading data from repository " + this.storageConfiguration.getName() + " for " + uuid + " - " + name, exception);

                        synchronized (this.lock) {
                            this.asyncLoginReject.add(uuid);
                        }

                        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, this.storageConfiguration.getLoginRejectMessage(uuid, name, LoginRejectReason.ERROR));
                    }
                }).build(this.getPlugin())
                .bindWith(containerObj);

        Events.subscribe(AsyncPlayerPreLoginEvent.class)
                .priority(EventPriority.MONITOR)
                .listen(event -> {
                    synchronized (this.lock) {
                        if (this.asyncLoginReject.remove(event.getUniqueId())) {
                            if (event.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) {
                                Log.warn("Player pre-login were re-allowed for " + event.getUniqueId() + " by plugins. rejecting it once again");
                                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "");
                            }
                        }
                    }
                }).build(this.getPlugin())
                .bindWith(containerObj);

        Events.subscribe(PlayerLoginEvent.class)
                .priority(EventPriority.LOWEST)
                .listen(event -> {
                    // ensure data is loaded when logging in
                    Player player = event.getPlayer();

                    if (this.isDebugging()) {
                        Log.info("Processing login for Player " + player.getUniqueId() + " - " + player.getName());
                    }

                    final T t = this.find(player.getUniqueId());

                    // It's doesn't loaded for some reason, reject login
                    if (t == null) {
                        synchronized (this.lock) {
                            this.syncLoginReject.add(player.getUniqueId());
                        }

                        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, this.storageConfiguration.getLoginRejectMessage(player.getUniqueId(), player.getName(), LoginRejectReason.DATA_UNLOADED));
                        return;
                    }

                    this.onLoadedMain(player, t);
                }).build(this.getPlugin())
                .bindWith(containerObj);

        Events.subscribe(PlayerLoginEvent.class)
                .priority(EventPriority.MONITOR)
                .listen(event -> {
                    synchronized (this.lock) {
                        if (this.syncLoginReject.remove(event.getPlayer().getUniqueId())) {
                            if (event.getResult() == PlayerLoginEvent.Result.ALLOWED) {
                                Log.warn("Player login were re-allowed for " + event.getPlayer().getUniqueId() + " by plugins. rejecting it once again");
                                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "");
                            }
                        }
                    }
                }).build(this.getPlugin())
                .bindWith(containerObj);

        Events.subscribe(PlayerQuitEvent.class)
                .priority(EventPriority.MONITOR)
                .listen(event -> {
                    final Player player = event.getPlayer();
                    if (!this.storageConfiguration.shouldUnloadOnQuit(player)) {
                        return;
                    }
                    this.unload(player.getUniqueId());
                })
                .build(this.getPlugin())
                .bindWith(containerObj);
    }

    @Override
    public List<T> findAll() {
        return Lists.newArrayList(this.storedObjects.values());
    }
}
