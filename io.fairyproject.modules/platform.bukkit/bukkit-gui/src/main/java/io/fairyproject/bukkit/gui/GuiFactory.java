package io.fairyproject.bukkit.gui;

import io.fairyproject.bukkit.events.BukkitEventNode;
import io.fairyproject.container.InjectableComponent;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

@InjectableComponent
@RequiredArgsConstructor
public class GuiFactory {

    private final BukkitEventNode bukkitEventNode;

    public Gui create(@NotNull Component component) {
        return new Gui(bukkitEventNode, component);
    }

}
