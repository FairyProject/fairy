package io.fairyproject.container.object.lifecycle.impl.annotation;

import io.fairyproject.container.PreDestroy;
import io.fairyproject.container.object.ContainerObj;

import javax.annotation.PostConstruct;

public class CommonLifeCycleAnnotationProcessor extends LifeCycleAnnotationProcessor {

    public CommonLifeCycleAnnotationProcessor(ContainerObj containerObj) {
        super(containerObj);
        this.setPostInitAnnotation(PostConstruct.class);
        this.setPreDestroyAnnotation(PreDestroy.class);
    }

}
