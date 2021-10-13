package org.fairy.mc.message;

import org.fairy.mc.MCPlayer;

public interface MCMessage {

    static MCMessage text(String string) {
        return new TextMCMessage(string);
    }

    String get(MCPlayer player);

    default MCMessage color() {
        return this.color('&');
    }

    default MCMessage color(char translateCode) {
        return new ColoredMCMessage(translateCode, this);
    }

}
