package org.fairy.mc.message;

import net.md_5.bungee.api.ChatColor;
import org.fairy.mc.MCPlayer;

public class ColoredMCMessage implements MCMessage {

    private final char translateCode;
    private final MCMessage message;

    public ColoredMCMessage(char translateCode, MCMessage message) {
        this.translateCode = translateCode;
        this.message = message;
    }

    @Override
    public String get(MCPlayer player) {
        return ChatColor.translateAlternateColorCodes(translateCode, this.message.get(player));
    }

    @Override
    public MCMessage color(char translateCode) {
        return this;
    }
}
