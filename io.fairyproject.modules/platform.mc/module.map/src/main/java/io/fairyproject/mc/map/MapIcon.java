package io.fairyproject.mc.map;


import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Data
@Accessors(fluent = true)
public class MapIcon {

    @With
    private final byte x, y, rotation, type;

}
