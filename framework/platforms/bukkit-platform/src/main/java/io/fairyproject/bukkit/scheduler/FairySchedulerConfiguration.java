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

package io.fairyproject.bukkit.scheduler;

import io.fairyproject.Debug;
import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.bukkit.plugin.impl.RootJavaPluginIdentifier;
import io.fairyproject.bukkit.scheduler.bukkit.BukkitSchedulerProvider;
import io.fairyproject.bukkit.scheduler.folia.FoliaSchedulerProvider;
import io.fairyproject.container.InjectableComponent;
import io.fairyproject.container.configuration.Configuration;
import io.fairyproject.mc.scheduler.MCSchedulerProvider;
import org.bukkit.plugin.java.JavaPlugin;

@Configuration
public class FairySchedulerConfiguration {

    @InjectableComponent
    public MCSchedulerProvider mcSchedulerProvider() {
        JavaPlugin javaPlugin = RootJavaPluginIdentifier.getInstance().findByClass(FairyBukkitPlatform.class);

        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");

            Debug.log("Using FoliaSchedulerProvider");
            return new FoliaSchedulerProvider(javaPlugin);
        } catch (ClassNotFoundException e) {
            Debug.log("Using BukkitSchedulerProvider");
            return new BukkitSchedulerProvider(javaPlugin);
        }
    }

}
