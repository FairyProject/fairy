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

package io.fairyproject.sidebar;

import net.kyori.adventure.text.Component;
import io.fairyproject.mc.MCPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

@Deprecated
public interface SidebarAdapter {

    default void onBoardCreate(MCPlayer player, Sidebar board) {
        // optional overwrite
    }


    Component getTitle(MCPlayer player);

    List<Component> getLines(MCPlayer player);

    /**
     *
     * This will only work when this adapter is highest priority
     *
     * @return the update tick, -1 if you want lower priority to decide it or use default one
     */
    default int tick() {
        return -1;
    }

    default int priority() { return 0; }

    static SidebarProvider asProvider(@NotNull SidebarAdapter adapter) {
        return new SidebarProvider() {
            @Override
            public Component getTitle(@NotNull MCPlayer mcPlayer) {
                return adapter.getTitle(mcPlayer);
            }

            @Override
            public List<SidebarLine> getLines(@NotNull MCPlayer mcPlayer) {
                List<Component> lines = adapter.getLines(mcPlayer);
                return lines == null ? null : lines.stream()
                        .map(SidebarLine::of)
                        .collect(Collectors.toList());
            }

            @Override
            public int getPriority() {
                return adapter.priority();
            }

            @Override
            public void onSidebarShown(@NotNull MCPlayer mcPlayer, @NotNull Sidebar sidebar) {
                adapter.onBoardCreate(mcPlayer, sidebar);
            }
        };
    }

}
