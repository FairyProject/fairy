package io.fairyproject.mc.actionbar;

import io.fairyproject.mc.MCPlayer;
import net.kyori.adventure.text.Component;

public interface ActionbarAdapter {

    Component build(MCPlayer player);

    int ticks();

    int priority();

}
