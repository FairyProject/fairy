package io.fairyproject.module;

import io.fairyproject.util.terminable.Terminable;
import io.fairyproject.util.terminable.TerminableConsumer;
import io.fairyproject.util.terminable.composite.CompositeTerminable;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class Module implements Terminable, TerminableConsumer {

    private final String name;
    private final String classPath;
    private final ClassLoader classLoader;
    private final List<Module> dependModules;

    private boolean abstraction;
    private boolean closed;

    private final CompositeTerminable compositeTerminable = CompositeTerminable.create();
    private final AtomicInteger refCount;

    public Module(String name, String classPath, ClassLoader classLoader) {
        this.name = name;
        this.classPath = classPath;
        this.classLoader = classLoader;
        this.dependModules = new ArrayList<>();
        this.refCount = new AtomicInteger(0);
    }

    public int addRef() {
        return this.refCount.incrementAndGet();
    }

    public int removeRef() {
        return this.refCount.decrementAndGet();
    }

    @Override
    public void close() throws Exception {
        this.closed = true;
        this.compositeTerminable.close();
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @NotNull
    @Override
    public <T extends AutoCloseable> T bind(@NotNull T terminable) {
        return this.compositeTerminable.bind(terminable);
    }
}
