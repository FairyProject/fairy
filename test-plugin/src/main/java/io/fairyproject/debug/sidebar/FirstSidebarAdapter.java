package io.fairyproject.debug.sidebar;

import io.fairyproject.container.PostInitialize;
import io.fairyproject.libs.kyori.adventure.text.Component;
import io.fairyproject.libs.kyori.adventure.text.format.NamedTextColor;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.sidebar.SidebarAdapter;

import java.util.Collections;
import java.util.List;

@io.fairyproject.container.Component
public class FirstSidebarAdapter implements SidebarAdapter {

    @PostInitialize
    public void onPostInitialize() {
        System.out.println("========= init");
    }

    @Override
    public Component getTitle(MCPlayer mcPlayer) {
        System.out.println("hi");
        return Component.text("First", NamedTextColor.AQUA);
    }

    @Override
    public List<Component> getLines(MCPlayer mcPlayer) {
        if (Math.random() > 0.5) {
            return Collections.singletonList(Component.text("a"));
        }
        return null;
    }

    @Override
    public int priority() {
        return 1;
    }
}
