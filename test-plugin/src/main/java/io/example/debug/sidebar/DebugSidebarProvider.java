package io.example.debug.sidebar;

import io.fairyproject.container.InjectableComponent;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.sidebar.SidebarLine;
import io.fairyproject.sidebar.SidebarProvider;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//@InjectableComponent
public class DebugSidebarProvider implements SidebarProvider {

    private int ticks = 0;
    private int disabled = 0;

    @Override
    public @Nullable Component getTitle(@NotNull MCPlayer mcPlayer) {
        return Component.text("Debug Sidebar");
    }

    @Override
    public @Nullable List<SidebarLine> getLines(@NotNull MCPlayer mcPlayer) {
        if (disabled > 0) {
            disabled--;
            return Collections.emptyList();
        }

        ticks++;
        List<SidebarLine> lines = new ArrayList<>();
        for (int i = 0; i < ticks; i++) {
            lines.add(SidebarLine.of(Component.text("Line " + i)));
        }
        if (ticks >= 15) {
            ticks = 0;
            disabled = 10;
        }
        return lines;
    }
}
