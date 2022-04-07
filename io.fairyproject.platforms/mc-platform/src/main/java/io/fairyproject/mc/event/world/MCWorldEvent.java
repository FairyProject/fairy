package io.fairyproject.mc.event.world;

import io.fairyproject.event.Event;
import io.fairyproject.mc.MCWorld;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Event
@RequiredArgsConstructor
@Getter
public class MCWorldEvent {

    private final MCWorld world;

}
