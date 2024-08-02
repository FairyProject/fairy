package io.example.debug.tablist;

import io.fairyproject.container.InjectableComponent;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.tablist.TabColumn;
import io.fairyproject.mc.tablist.TablistAdapter;
import io.fairyproject.mc.tablist.util.TabSlot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

@InjectableComponent
public class DebugTablistAdapter implements TablistAdapter {

    private int index = 0;

    @Override
    public @Nullable Set<TabSlot> getSlots(MCPlayer player) {
        return Collections.singleton(new TabSlot()
                .slot(1)
                .column(TabColumn.LEFT)
                .text(Component.text("Test " + index++))
                .ping(0)
        );
    }

    @Override
    public @Nullable Component getFooter(MCPlayer player) {
        return Component.text("GOGOGO", NamedTextColor.BLACK);
    }

    @Override
    public @Nullable Component getHeader(MCPlayer player) {
        return Component.text("FFF", NamedTextColor.GOLD);
    }
}
