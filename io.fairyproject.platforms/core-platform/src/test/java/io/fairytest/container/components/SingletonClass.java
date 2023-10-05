package io.fairytest.container.components;

import io.fairyproject.container.*;
import lombok.Getter;

@InjectableComponent
@Getter
public class SingletonClass implements InterfaceClass {

    @Autowired
    public static SingletonClass STATIC_WIRED;

    private final long constructMs;
    private long preInitializeMs = -1;
    private long postInitializeMs = -1;
    private long preDestroyMs = -1;
    private long postDestroyMs = -1;

    private final Thread constructThread;
    private Thread preInitializeThread;
    private Thread postInitializeThread;
    private Thread preDestroyThread;
    private Thread postDestroyThread;

    @ContainerConstruct
    public SingletonClass() {
        this.constructMs = System.nanoTime();
        this.constructThread = Thread.currentThread();
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
