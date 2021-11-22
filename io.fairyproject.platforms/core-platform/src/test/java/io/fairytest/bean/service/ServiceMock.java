package io.fairytest.bean.service;

import io.fairyproject.bean.*;
import io.fairyproject.jackson.JacksonService;
import lombok.Getter;

@Service(name = "fairy:test")
public class ServiceMock {

    @Autowired
    public static ServiceMock STATIC_WIRED;

    @Autowired
    public JacksonService jacksonService;

    public BeanContext beanContext;

    @Getter
    private long construct = -1, preInitialize = -1, postInitialize = -1, preDestroy = -1, postDestroy = -1;

    @BeanConstructor
    public ServiceMock(BeanContext beanContext) {
        this.construct = System.currentTimeMillis();
        this.beanContext = beanContext;
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
