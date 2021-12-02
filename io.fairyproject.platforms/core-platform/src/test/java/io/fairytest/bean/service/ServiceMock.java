package io.fairytest.bean.service;

import io.fairyproject.container.*;
import io.fairyproject.jackson.JacksonService;
import lombok.Getter;

@Service(name = "fairy:test")
public class ServiceMock {

    @Autowired
    public static ServiceMock STATIC_WIRED;

    @Autowired
    public JacksonService jacksonService;

    public ContainerContext containerContext;

    @Getter
    private long construct = -1, preInitialize = -1, postInitialize = -1, preDestroy = -1, postDestroy = -1;

    @ContainerConstruct
    public ServiceMock(ContainerContext containerContext) {
        this.construct = System.nanoTime();
        this.containerContext = containerContext;
    }

    @PreInitialize
    public void onPreInitialize() {
        this.preInitialize = System.nanoTime();
    }

    @PostInitialize
    public void onPostInitialize() {
        this.postInitialize = System.nanoTime();
    }

    @PreDestroy
    public void onPreDestroy() {
        this.preDestroy = System.nanoTime();
    }

    @PostDestroy
    public void onPostDestroy() {
        this.postDestroy = System.nanoTime();
    }

}
