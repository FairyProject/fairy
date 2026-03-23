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

package io.example.hytale;

import io.fairyproject.plugin.Plugin;

/**
 * A simple test plugin for Hytale platform.
 */
public class HytaleTestPlugin extends Plugin {

    @Override
    public void onInitial() {
        System.out.println("[HytaleTestPlugin] onInitial called - Plugin is initializing!");
    }

    @Override
    public void onPluginEnable() {
        System.out.println("[HytaleTestPlugin] onPluginEnable called - Plugin is now enabled!");
        System.out.println("[HytaleTestPlugin] Hello from Hytale!");
    }

    @Override
    public void onPluginDisable() {
        System.out.println("[HytaleTestPlugin] onPluginDisable called - Plugin is being disabled!");
    }
}
