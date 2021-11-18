package io.fairyproject.mc.event;

import io.fairyproject.event.Event;
import io.fairyproject.mc.MCPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Event
@RequiredArgsConstructor
@Getter
public class MCPlayerEvent {

    private final MCPlayer player;

}
