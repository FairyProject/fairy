package io.fairyproject.container.object.lifecycle.impl.annotation;

import io.fairyproject.container.PostDestroy;
import io.fairyproject.container.PostInitialize;
import io.fairyproject.container.PreDestroy;
import io.fairyproject.container.PreInitialize;
import io.fairyproject.container.object.ContainerObj;

public class FairyLifeCycleAnnotationProcessor extends LifeCycleAnnotationProcessor {

    public FairyLifeCycleAnnotationProcessor(ContainerObj containerObj) {
        super(containerObj);
        this.setPreInitAnnotation(PreInitialize.class);
        this.setPostInitAnnotation(PostInitialize.class);
        this.setPreDestroyAnnotation(PreDestroy.class);
        this.setPostDestroyAnnotation(PostDestroy.class);
    }

}
