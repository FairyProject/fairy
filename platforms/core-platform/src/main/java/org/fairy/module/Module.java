package org.fairy.module;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class Module {

    private final String name;
    private final List<Module> dependModules;

    private boolean abstraction;

    private final AtomicInteger refCount;

    public Module(String name) {
        this.name = name;
        this.dependModules = new ArrayList<>();
        this.refCount = new AtomicInteger(0);
    }

    public int addRef() {
        return this.refCount.incrementAndGet();
    }

    public int removeRef() {
        return this.refCount.decrementAndGet();
    }

}
