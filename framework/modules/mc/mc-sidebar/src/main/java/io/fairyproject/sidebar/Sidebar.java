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

import io.fairyproject.Fairy;
import io.fairyproject.data.MetaKey;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.sidebar.handler.SidebarHandler;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class Sidebar {

    public static final MetaKey<Sidebar> METADATA_TAG = MetaKey.create(Fairy.METADATA_PREFIX + "Scoreboard", Sidebar.class);
    private static final int MAX_LINES = 15;

    private final MCPlayer player;
    private final SidebarHandler sidebarHandler;
    private final SidebarLine[] lines;
    private Component title;

    private int ticks;
    private boolean available;
    private SidebarProvider provider;

    public Sidebar(@NotNull MCPlayer player, @NotNull SidebarHandler sidebarHandler) {
        this.player = player;
        this.sidebarHandler = sidebarHandler;
        this.lines = new SidebarLine[16];
    }

    public void setTitle(@NotNull Component title) {
        if (Objects.equals(this.title, title))
            return;

        this.title = title;
        if (!available) {
            this.available = true;
            this.sidebarHandler.sendObjective(this);
        } else {
            this.sidebarHandler.sendTitle(this);
        }
    }

    public void setLines(List<SidebarLine> lines) {
        int lineCount = 1;

        for (int i = lines.size() - 1; i >= 0; --i) {
            this.setLine(lineCount, lines.get(i));
            lineCount++;
        }

        for (int i = lines.size(); i < 15; i++) {
            if (this.lines[lineCount] != null) {
                this.clear(lineCount);
            }
            lineCount++;
        }
    }

    private void setLine(int index, @NotNull SidebarLine line) {
        if (index < 1 || index > MAX_LINES)
            return;

        if (Objects.equals(this.lines[index], line))
            return;

        this.sidebarHandler.sendLine(this, index, line);
        this.lines[index] = line;
    }

    @Nullable
    public SidebarLine getLine(int line) {
        if (line < 1 || line > 15)
            return null;

        return this.lines[line];
    }

    public void clear(int line) {
        if (line < 1 || line > 15)
            return;

        if (lines[line] == null)
            return;

        this.sidebarHandler.removeLine(this, line);
        this.lines[line] = null;
    }

    public void remove() {
        if (!this.available)
            return;

        this.available = false;

        for (int line = 1; line < 15; line++) {
            this.clear(line);
        }

        this.title = null;
        this.provider = null;
        this.sidebarHandler.removeObjective(this);
    }

}
