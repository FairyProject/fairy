package io.fairyproject.debug.tablist;

import io.fairyproject.libs.kyori.adventure.text.Component;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.tablist.TablistAdapter;
import io.fairyproject.mc.tablist.util.TabColumn;
import io.fairyproject.mc.tablist.util.TabSlot;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@io.fairyproject.container.Component
public class TestTablistAdapter implements TablistAdapter {
    @Override
    public Set<TabSlot> getSlots(MCPlayer mcPlayer) {
        Set<TabSlot> slots = new HashSet<>();

        slots.add(new TabSlot()
                .text(Component.text("e"))
                        .slot(5)
                .ping(1)
                .column(TabColumn.LEFT));
        slots.add(new TabSlot()
                .text(Component.text("e"))
                .slot(5)
                .ping(1)
                .column(TabColumn.MIDDLE));
        slots.add(new TabSlot()
                .text(Component.text("e"))
                .slot(5)
                .ping(1)
                .column(TabColumn.RIGHT));
        slots.add(new TabSlot()
                .text(Component.text("e"))
                .slot(5)
                .ping(1)
                .column(TabColumn.FAR_RIGHT));

        return slots;
    }

    @Override
    public Component getFooter(MCPlayer mcPlayer) {
        return null;
    }

    @Override
    public Component getHeader(MCPlayer mcPlayer) {
        return null;
    }
}
