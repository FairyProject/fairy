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

package io.fairyproject.mc.tablist;

import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.tablist.util.Skin;
import io.fairyproject.mc.tablist.util.TabSlot;
import io.fairyproject.mc.version.MCVersion;
import lombok.Getter;
import net.kyori.adventure.text.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public class Tablist {

    private final MCPlayer player;
    private final TablistService tablistService;
    private final TablistSender tablistSender;
    private final Set<TabEntry> entries = new HashSet<>();
    private final AtomicBoolean shown;

    private Component header;
    private Component footer;


    public Tablist(MCPlayer player, TablistService tablistService, TablistSender tablistSender) {
        this.player = player;
        this.tablistService = tablistService;
        this.tablistSender = tablistSender;
        this.shown = new AtomicBoolean(false);

        this.setup();
    }

    private void setup() {
        this.entries.clear();

        final int possibleSlots = player.getVersion().isLowerThan(MCVersion.of(8)) ? 60 : 80;

        for (int i = 1; i <= possibleSlots; i++) {
            final TabColumn tabColumn = TabColumn.getFromSlot(player, i);
            if (tabColumn == null) {
                continue;
            }

            TabEntry tabEntry = new TabEntry(String.format("%03d", i) + "|Tab", UUID.randomUUID(), Component.empty(), this, Skin.GRAY, tabColumn, tabColumn.getNumber(player, i), 0);
            entries.add(tabEntry);
        }
    }

    private void show() {
        if (!this.shown.compareAndSet(false, true))
            return;

        this.tablistSender.addFakePlayer(this, this.entries);
    }

    private void hide() {
        if (!this.shown.compareAndSet(true, false))
            return;

        this.tablistSender.removeFakePlayer(this, this.entries);
        for (TabEntry tabEntry : this.entries) {
            this.tablistSender.updateFakeName(this, tabEntry, Component.empty());
            this.tablistSender.updateFakeLatency(this, tabEntry, 0);
            this.tablistSender.updateFakeSkin(this, tabEntry, Skin.GRAY);
        }

        this.header = null;
        this.footer = null;
        this.tablistSender.updateHeaderAndFooter(this, Component.empty(), Component.empty());
    }

    public void update() {
        Set<TabEntry> previous = new HashSet<>(entries);

        Set<TabSlot> current = this.tablistService.getSlots(player);
        if (current == null || current.isEmpty()) {
            this.hide();
            return;
        }

        this.show();

        for (TabSlot tabSlot : current) {
            TabEntry tabEntry = getEntry(tabSlot.getColumn(), tabSlot.getSlot());

            if (tabEntry != null) {
                previous.remove(tabEntry);

                this.tablistSender.updateFakeLatency(this, tabEntry, tabSlot.getPing());
                this.tablistSender.updateFakeName(this, tabEntry, tabSlot.getText());
                this.tablistSender.updateFakeSkin(this, tabEntry, tabSlot.getSkin());
            }
        }

        for (TabEntry tabEntry : previous) {
            this.tablistSender.updateFakeName(this, tabEntry, Component.empty());
            this.tablistSender.updateFakeLatency(this, tabEntry, 0);
            this.tablistSender.updateFakeSkin(this, tabEntry, Skin.GRAY);
        }

        previous.clear();

        Component headerNow = this.tablistService.getHeader(player);
        Component footerNow = this.tablistService.getFooter(player);

        if (!Objects.equals(this.header, headerNow) || !Objects.equals(this.footer, footerNow)) {
            this.tablistSender.updateHeaderAndFooter(this, headerNow, footerNow);
            this.header = headerNow;
            this.footer = footerNow;
        }
    }

    public TabEntry getEntry(TabColumn column, int slot) {
        for (TabEntry entry : entries) {
            if (entry.getColumn() == column && entry.getSlot() == slot) {
                return entry;
            }
        }
        throw new IllegalArgumentException("No entry found for column " + column + " and slot " + slot);
    }
}
