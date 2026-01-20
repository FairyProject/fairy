/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
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

package io.fairyproject.bootstrap.hytale;

import com.google.gson.JsonObject;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import io.fairyproject.bootstrap.PluginClassInitializerFinder;
import io.fairyproject.bootstrap.PluginFileReader;
import io.fairyproject.bootstrap.instance.PluginInstance;
import io.fairyproject.bootstrap.internal.FairyInternalIdentityMeta;
import io.fairyproject.bootstrap.platform.PlatformBootstrap;
import lombok.AccessLevel;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@FairyInternalIdentityMeta
public final class HytalePlugin extends JavaPlugin {

    public static HytalePlugin INSTANCE;

    private final PluginInstance instance;
    private final PluginFileReader pluginFileReader;
    private final PlatformBootstrap bootstrap;
    private Runnable shutdownAction;

    @Setter(AccessLevel.PACKAGE)
    private boolean loaded;

    /**
     * Constructor with dependency injection for testing purposes.
     *
     * @param init             the Hytale plugin init object
     * @param instance         the plugin instance
     * @param pluginFileReader the plugin file reader
     * @param bootstrap        the platform bootstrap
     * @param shutdownAction   the action to run on shutdown (nullable, will use default if null)
     */
    public HytalePlugin(@NotNull JavaPluginInit init,
                        PluginInstance instance,
                        PluginFileReader pluginFileReader,
                        PlatformBootstrap bootstrap,
                        Runnable shutdownAction) {
        super(init);
        this.instance = instance;
        this.pluginFileReader = pluginFileReader;
        this.bootstrap = bootstrap;
        this.shutdownAction = shutdownAction;
    }

    /**
     * Default constructor for Hytale server.
     *
     * @param init the Hytale plugin init object
     */
    public HytalePlugin(@NotNull JavaPluginInit init) {
        this(
                init,
                new HytalePluginInstance(PluginClassInitializerFinder.find()),
                new PluginFileReader(),
                new HytalePlatformBootstrap(),
                null // will be initialized lazily
        );
    }

    /**
     * Get the shutdown action to be used by the platform.
     * Lazily initializes to server stop if not set.
     *
     * @return the shutdown action
     */
    public Runnable getShutdownAction() {
        if (this.shutdownAction == null) {
            // Use reflection to call server stop to avoid compile-time API dependency issues
            this.shutdownAction = () -> {
                try {
                    Object server = this.getClass().getMethod("getServer").invoke(this);
                    if (server != null) {
                        server.getClass().getMethod("stop").invoke(server);
                    }
                } catch (Exception e) {
                    System.err.println("[Fairy] Failed to stop server: " + e.getMessage());
                }
            };
        }
        return this.shutdownAction;
    }

    /**
     * Called by Hytale during plugin setup phase.
     * Maps to Bukkit's onLoad() lifecycle.
     * Performs: preload + load + instance.onLoad
     */
    @Override
    protected void setup() {
        if (this.loaded)
            return;
        INSTANCE = this;

        if (!this.bootstrap.preload()) {
            System.err.println("[Fairy] Failed to boot fairy! check stacktrace for the reason of failure!");
            this.getShutdownAction().run();
            return;
        }

        JsonObject jsonObject = pluginFileReader.read(this.getClass());
        this.instance.init(jsonObject);
        this.bootstrap.load(this.instance.getPlugin());
        this.instance.onLoad();

        this.loaded = true;
    }

    /**
     * Called by Hytale during plugin start phase.
     * Maps to Bukkit's onEnable() lifecycle.
     * Performs: enable + instance.onEnable
     */
    @Override
    protected void start() {
        if (!this.loaded)
            throw new IllegalStateException("Plugin not loaded yet!");

        this.bootstrap.enable();
        this.instance.onEnable();
    }

    /**
     * Called by Hytale during plugin shutdown phase.
     * Maps to Bukkit's onDisable() lifecycle.
     * Performs: instance.onDisable + disable
     */
    @Override
    protected void shutdown() {
        if (!this.loaded)
            return;

        this.instance.onDisable();
        this.bootstrap.disable();
    }
}
