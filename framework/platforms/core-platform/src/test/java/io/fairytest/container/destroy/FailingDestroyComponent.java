package io.fairytest.container.destroy;

import io.fairyproject.container.InjectableComponent;
import io.fairyproject.container.PostDestroy;
import io.fairyproject.container.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Depends on {@link DestroyTrackingComponent}, so it is destroyed before it.
 */
@InjectableComponent
@RequiredArgsConstructor
@Getter
public class FailingDestroyComponent {

    private final DestroyTrackingComponent dependency;

    @PreDestroy
    public void onPreDestroy() {
        throw new IllegalStateException("pre destroy failure");
    }

    @PostDestroy
    public void onPostDestroy() {
        throw new IllegalStateException("post destroy failure");
    }

}
