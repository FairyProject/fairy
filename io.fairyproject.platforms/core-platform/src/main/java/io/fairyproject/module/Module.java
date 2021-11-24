package io.fairyproject.module;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.fairyproject.container.Autowired;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.util.terminable.Terminable;
import io.fairyproject.util.terminable.TerminableConsumer;
import io.fairyproject.util.terminable.composite.CompositeTerminable;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class Module implements Terminable, TerminableConsumer {

    @Autowired
    private static ModuleService MODULE_SERVICE;

    private final String name;
    private final String classPath;
    private final ClassLoader classLoader;
    private final Plugin plugin;
    private final List<Module> dependModules;
    // <module, package>
    private final Multimap<String, String> exclusives;
    private transient final Multimap<String, String> excluded;

    private final Path notShadedPath;
    private final Path shadedPath;

    private boolean abstraction;
    private boolean closed;

    private final CompositeTerminable compositeTerminable = CompositeTerminable.create();
    private final AtomicInteger refCount;

    public Module(String name, String classPath, ClassLoader classLoader, Plugin plugin, Path notShadedPath, Path shadedPath) {
        this.name = name;
        this.classPath = classPath;
        this.classLoader = classLoader;
        this.plugin = plugin;
        this.notShadedPath = notShadedPath;
        this.shadedPath = shadedPath;
        this.dependModules = new ArrayList<>();
        this.exclusives = HashMultimap.create();
        this.excluded = HashMultimap.create();
        this.refCount = new AtomicInteger(0);
    }

    @Nullable
    public Collection<String> releaseExclusive(Module module) {
        return this.excluded.removeAll(module.getName());
    }

    public Collection<String> placeExclusive(Module module) {
        final Collection<String> packages = this.exclusives.get(module.getName());
        for (String curPackage : packages) {
            this.excluded.put(module.getName(), curPackage);
        }
        return packages;
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
