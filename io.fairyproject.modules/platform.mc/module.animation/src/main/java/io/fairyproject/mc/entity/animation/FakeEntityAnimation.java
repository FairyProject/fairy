package io.fairyproject.mc.entity.animation;

import io.fairyproject.mc.MCEntity;
import io.fairyproject.mc.Viewable;
import org.jetbrains.annotations.NotNull;

public interface FakeEntityAnimation extends Viewable {

    static FakeEntityAnimation death(@NotNull MCEntity entity) {
        return new FakeEntityDeathAnimation(entity);
    }

    @NotNull MCEntity getEntity();

    /**
     * add nearby players into viewer
     *
     * @param viewDistance the view distance in chunks
     */
    void addNearbyViewers(int viewDistance);

    void start();

}
