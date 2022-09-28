package io.fairyproject.mc.event;

import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.MCWorld;
import io.fairyproject.mc.event.trait.MCPlayerEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class MCPlayerChangedWorldEvent implements MCPlayerEvent {

    private final MCPlayer player;
    private final MCWorld worldFrom;
    private final MCWorld worldTo;

}
