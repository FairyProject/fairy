package io.fairyproject.mc.map;

import io.fairyproject.mc.map.packet.MapIcon;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Collection;


@Data
@RequiredArgsConstructor
@Accessors(fluent = true)
public class RenderData {

    private final int id;
    private final Collection<MapIcon> icons;
    private final byte[] colors;
    private final int x;
    private final int y;
    private final int width;
    private final int height;

}
