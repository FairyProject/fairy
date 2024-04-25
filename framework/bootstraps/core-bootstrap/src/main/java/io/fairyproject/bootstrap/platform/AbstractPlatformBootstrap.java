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

package io.fairyproject.bootstrap.platform;

import io.fairyproject.FairyPlatform;
import io.fairyproject.bootstrap.type.PlatformType;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginManager;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractPlatformBootstrap implements PlatformBootstrap {

    private FairyPlatform fairyPlatform;

    @Override
    public final boolean preload() {
        if (FairyPlatform.class.getClassLoader() != this.getClass().getClassLoader()) {
            return true;
        }
        if (FairyPlatform.INSTANCE != null) {
            return true;
        }

        try {
            this.fairyPlatform = this.createPlatform();
            this.fairyPlatform.preload();
        } catch (Throwable throwable) {
            this.onFailure(throwable);
            return false;
        }

        return true;
    }

    @Override
    public final void load(Plugin plugin) {
        if (this.fairyPlatform == null) {
            return;
        }
        try {
            this.fairyPlatform.load(plugin);
        } catch (Throwable throwable) {
            this.onFailure(throwable);
        }
    }

    @Override
    public final void enable() {
        if (this.fairyPlatform == null) {
            return;
        }
        this.fairyPlatform.enable();
    }

    @Override
    public final void disable() {
        if (this.fairyPlatform == null) {
            return;
        }
        this.fairyPlatform.disable();
        PluginManager.INSTANCE.unload();
    }

    protected abstract void onFailure(@Nullable Throwable throwable);

    protected abstract PlatformType getPlatformType();

    protected abstract FairyPlatform createPlatform();

}
