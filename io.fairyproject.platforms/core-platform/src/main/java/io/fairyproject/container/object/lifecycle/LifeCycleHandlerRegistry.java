package io.fairyproject.container.object.lifecycle;


import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.container.object.lifecycle.impl.LifeCycleHandlerInterface;
import io.fairyproject.container.object.lifecycle.impl.annotation.CommonLifeCycleAnnotationProcessor;
import io.fairyproject.container.object.lifecycle.impl.annotation.FairyLifeCycleAnnotationProcessor;

public class LifeCycleHandlerRegistry {

    public void handle(ContainerObj obj) {
        // TODO: annotation to add/overwrite LifeCycleHandlers

        this.handleDefaults(obj);
    }

    private void handleDefaults(ContainerObj obj) {
        if (ILifeCycle.class.isAssignableFrom(obj.type()))
            obj.addLifeCycleHandler(new LifeCycleHandlerInterface(obj));

        // Annotation processors
        obj.addLifeCycleHandler(new FairyLifeCycleAnnotationProcessor(obj));
        obj.addLifeCycleHandler(new CommonLifeCycleAnnotationProcessor(obj));
    }

}
