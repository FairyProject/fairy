package io.fairyproject.mc.event;

import io.fairyproject.event.Event;
import io.fairyproject.mc.MCPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class MCPlayerEvent implements Event {

    private final MCPlayer player;

}
