package io.fairyproject.mc.world;

import io.fairyproject.mc.MCEntity;
import io.fairyproject.mc.MCWorld;

import java.util.List;

public interface MCChunk {

    int x();

    int z();

    MCWorld world();

    List<MCEntity> entities();

}
