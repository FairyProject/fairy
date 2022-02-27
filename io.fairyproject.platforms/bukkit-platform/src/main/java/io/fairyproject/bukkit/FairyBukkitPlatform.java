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

package io.fairyproject.bukkit;

import com.github.retrooper.packetevents.PacketEvents;
import io.fairyproject.ExtendedClassLoader;
import io.fairyproject.FairyPlatform;
import io.fairyproject.bukkit.events.PostServicesInitialEvent;
import io.fairyproject.bukkit.impl.BukkitPluginHandler;
import io.fairyproject.bukkit.impl.BukkitTaskScheduler;
import io.fairyproject.bukkit.impl.ComponentHolderBukkitListener;
import io.fairyproject.bukkit.listener.events.Events;
import io.fairyproject.bukkit.mc.BukkitMCInitializer;
import io.fairyproject.bukkit.reflection.MinecraftReflection;
import io.fairyproject.bukkit.util.SpigotUtil;
import io.fairyproject.container.ComponentRegistry;
import io.fairyproject.library.Library;
import io.fairyproject.mc.MCInitializer;
import io.fairyproject.module.ModuleService;
import io.fairyproject.plugin.PluginManager;
import io.fairyproject.task.ITaskScheduler;
import io.fairyproject.util.terminable.TerminableConsumer;
import io.fairyproject.util.terminable.composite.CompositeTerminable;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class FairyBukkitPlatform extends FairyPlatform implements TerminableConsumer {

    private static final Logger LOGGER = LogManager.getLogger(FairyBukkitPlatform.class);

    public static FairyBukkitPlatform INSTANCE;
    public static Plugin PLUGIN;
    public static BukkitAudiences AUDIENCES;

    private final ExtendedClassLoader classLoader;
    private final File dataFolder;
    private final CompositeTerminable compositeTerminable;

    @NotNull
    @Override
    public <T extends AutoCloseable> T bind(@NotNull T terminable) {
        return this.compositeTerminable.bind(terminable);
    }

    public FairyBukkitPlatform(File dataFolder) {
        FairyPlatform.INSTANCE = this;
        this.dataFolder = dataFolder;
        this.compositeTerminable = CompositeTerminable.create();
        this.classLoader = new ExtendedClassLoader(this.getClass().getClassLoader());
    }

    @Override
    public void load() {
        super.load();

        PluginManager.initialize(new BukkitPluginHandler());
        ModuleService.init();
        MinecraftReflection.init();
        this.createMCInitializer().apply();
        PacketEvents.getAPI().load();
    }

    @Override
    public void enable() {
        AUDIENCES = BukkitAudiences.create(PLUGIN);

        SpigotUtil.init();
        ComponentRegistry.registerComponentHolder(new ComponentHolderBukkitListener());

        super.enable();
        ModuleService.INSTANCE.enable();
        PacketEvents.getAPI().getSettings().debug(false).bStats(false).checkForUpdates(true);
        PacketEvents.getAPI().init();
    }

    @Override
    public void disable() {
        PacketEvents.getAPI().terminate();
        
        super.disable();
    }

    @Override
    public void onPostServicesInitial() {
        Events.call(new PostServicesInitialEvent());
    }

    public MCInitializer createMCInitializer() {
        return new BukkitMCInitializer();
    }

    @Override
    public void saveResource(String name, boolean replace) {
        PLUGIN.saveResource(name, replace);
    }

    @Override
    public ExtendedClassLoader getClassloader() {
        return this.classLoader;
    }

    @Override
    public File getDataFolder() {
        return this.dataFolder;
    }

    @Override
    public void shutdown() {
        Bukkit.shutdown();
    }

    @Override
    public boolean isRunning() {
        return true; // TODO
    }

    @Override
    public boolean isMainThread() {
        return Bukkit.isPrimaryThread();
    }

    @Override
    public ITaskScheduler createTaskScheduler() {
        return new BukkitTaskScheduler();
    }

    @Override
    public Collection<Library> getDependencies() {
        Set<Library> libraries = new HashSet<>();
        if (SpigotUtil.SPIGOT_TYPE != SpigotUtil.SpigotType.IMANITY) {
            libraries.add(Library.FAST_UTIL);
        }
//        libraries.add(Library.ADVENTURE_API);
        return libraries;
    }
}
