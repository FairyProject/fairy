package io.fairyproject.debug.sidebar;

import io.fairyproject.libs.kyori.adventure.text.Component;
import io.fairyproject.libs.kyori.adventure.text.format.NamedTextColor;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.sidebar.SidebarAdapter;

import java.util.Collections;
import java.util.List;

@io.fairyproject.container.Component
public class SecondSidebarAdapter implements SidebarAdapter {
    @Override
    public Component getTitle(MCPlayer mcPlayer) {
        return Component.text("Second", NamedTextColor.BLUE);
    }

    @Override
    public List<Component> getLines(MCPlayer mcPlayer) {
        return Collections.singletonList(Component.text("hello"));
    }
}
