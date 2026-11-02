package io.fairytest.container.destroy;

import io.fairyproject.container.InjectableComponent;
import io.fairyproject.container.PostDestroy;
import io.fairyproject.container.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Fails with an {@link Error}, like a callback using a soft dependency that was already disabled.
 * Depends on {@link DestroyTrackingComponent}, so it is destroyed before it.
 */
@InjectableComponent
@RequiredArgsConstructor
@Getter
public class ErrorDestroyComponent {

    private final DestroyTrackingComponent dependency;

    @PreDestroy
    public void onPreDestroy() {
        throw new NoClassDefFoundError("pre destroy failure");
    }

    @PostDestroy
    public void onPostDestroy() {
        throw new NoClassDefFoundError("post destroy failure");
    }

}
