package io.fairyproject.mc.event.world;

import io.fairyproject.event.Event;
import io.fairyproject.mc.MCWorld;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class MCWorldEvent implements Event {

    private final MCWorld world;

}
