package io.fairyproject.mc.util.math;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CoordinateUtil {

    public int worldToChunk(int x) {
        return (int) x >> 4;
    }

    public int chunkToWorld(int x) {
        return x << 4;
    }

}
