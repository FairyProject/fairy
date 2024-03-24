package io.fairyproject.bukkit.gui.event;

import io.fairyproject.bukkit.gui.Gui;
import io.fairyproject.bukkit.listener.events.PlayerCallableEvent;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class GuiCloseEvent extends PlayerCallableEvent {

    private final Gui gui;

    public GuiCloseEvent(Player who, Gui gui) {
        super(who);
        this.gui = gui;
    }
}
