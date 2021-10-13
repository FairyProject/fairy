package org.fairy.mc.message;

import lombok.RequiredArgsConstructor;
import org.fairy.mc.MCPlayer;

@RequiredArgsConstructor
public class TextMCMessage implements MCMessage {

    private final String text;

    @Override
    public String get(MCPlayer player) {
        return this.text;
    }
}
