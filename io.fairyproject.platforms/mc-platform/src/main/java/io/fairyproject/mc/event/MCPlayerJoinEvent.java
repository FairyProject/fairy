package io.fairyproject.mc.event;

import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.event.trait.MCPlayerEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class MCPlayerJoinEvent implements MCPlayerEvent {

    private final MCPlayer player;
}
