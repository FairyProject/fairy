package io.fairyproject.bukkit.gui.event;

import io.fairyproject.bukkit.gui.Gui;
import io.fairyproject.bukkit.listener.events.PlayerCancellableEvent;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class GuiOpenEvent extends PlayerCancellableEvent {

    private final Gui gui;

    public GuiOpenEvent(Player who, Gui gui) {
        super(who);
        this.gui = gui;
    }
}
