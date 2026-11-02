package io.fairytest.container.destroy;

import io.fairyproject.container.InjectableComponent;
import io.fairyproject.container.PostDestroy;
import io.fairyproject.container.PreDestroy;
import lombok.Getter;

@InjectableComponent
@Getter
public class DestroyTrackingComponent {

    private boolean preDestroyed;
    private boolean postDestroyed;

    @PreDestroy
    public void onPreDestroy() {
        this.preDestroyed = true;
    }

    @PostDestroy
    public void onPostDestroy() {
        this.postDestroyed = true;
    }

}
