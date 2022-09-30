package io.fairyproject.mc.event;

import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.event.trait.MCPlayerEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MCPlayerJoinEvent implements MCPlayerEvent {

    private final MCPlayer player;
}
