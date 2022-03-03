package io.fairytest.container.service;

import io.fairyproject.container.*;
import io.fairyproject.jackson.JacksonService;
import lombok.Getter;

@Service
public class ServiceMock {

    @Autowired
    public static ServiceMock STATIC_WIRED;

    @Autowired
    public JacksonService jacksonService;

    public ContainerContext containerContext;

    @Getter
    private final long constructMs;
    @Getter
    private long preInitializeMs = -1, postInitializeMs = -1, preDestroyMs = -1, postDestroyMs = -1;

    @Getter
    private final Thread constructThread;
    @Getter
    private Thread preInitializeThread, postInitializeThread, preDestroyThread, postDestroyThread;

    @ContainerConstruct
    public ServiceMock(ContainerContext containerContext) {
        this.constructMs = System.nanoTime();
        this.constructThread = Thread.currentThread();
        this.containerContext = containerContext;
    }

    @PreInitialize
    public void onPreInitialize() {
        this.preInitializeMs = System.nanoTime();
        this.preInitializeThread = Thread.currentThread();
    }

    @PostInitialize
    public void onPostInitialize() {
        this.postInitializeMs = System.nanoTime();
        this.postInitializeThread = Thread.currentThread();
    }

    @PreDestroy
    public void onPreDestroy() {
        this.preDestroyMs = System.nanoTime();
        this.preDestroyThread = Thread.currentThread();
    }

    @PostDestroy
    public void onPostDestroy() {
        this.postDestroyMs = System.nanoTime();
        this.postDestroyThread = Thread.currentThread();
    }

}
