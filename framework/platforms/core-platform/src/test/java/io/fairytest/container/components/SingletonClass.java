package io.fairytest.container.components;

import io.fairyproject.container.*;
import lombok.Getter;
import org.junit.jupiter.api.Assertions;

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
        Assertions.assertNull(this.preInitializeThread, "PreInitialize should only be called once");
        this.preInitializeMs = System.nanoTime();
        this.preInitializeThread = Thread.currentThread();
    }

    @PostInitialize
    public void onPostInitialize() {
        Assertions.assertNull(this.postInitializeThread, "PostInitialize should only be called once");
        this.postInitializeMs = System.nanoTime();
        this.postInitializeThread = Thread.currentThread();
    }

    @PreDestroy
    public void onPreDestroy() {
        Assertions.assertNull(this.preDestroyThread, "PreDestroy should only be called once");
        this.preDestroyMs = System.nanoTime();
        this.preDestroyThread = Thread.currentThread();
    }

    @PostDestroy
    public void onPostDestroy() {
        Assertions.assertNull(this.postDestroyThread, "PostDestroy should only be called once");
        this.postDestroyMs = System.nanoTime();
        this.postDestroyThread = Thread.currentThread();
    }

}
