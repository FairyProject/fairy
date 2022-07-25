package io.fairyproject.mc.map;


import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Data
@Accessors(fluent = true)
@With
public class MapIcon {

    private final byte x;
    private final byte y;
    private final byte rotation;
    private final byte type;

}
