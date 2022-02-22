package io.fairyproject.container.scanner;

import io.fairyproject.container.Autowired;
import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.object.ContainerObject;
import io.fairyproject.util.SimpleTiming;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class ClassPathScanner {

    public static ContainerContext CONTAINER_CONTEXT = ContainerContext.INSTANCE;

    public static void log(String msg, Object... replacement) {
        ContainerContext.log(msg, replacement);
    }

    public static SimpleTiming logTiming(String msg) {
        return ContainerContext.logTiming(msg);
    }

    protected String prefix = "";
    protected String scanName;
    protected final List<String> classPaths = new ArrayList<>();
    protected final List<String> excludedPackages = new ArrayList<>();
    protected final List<URL> urls = new ArrayList<>();
    protected final List<ClassLoader> classLoaders = new ArrayList<>();

    protected final List<ContainerObject> included = new ArrayList<>();

    public ClassPathScanner prefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public ClassPathScanner name(String name) {
        this.scanName = name;
        return this;
    }

    public ClassPathScanner classPath(String... classPath) {
        this.classPaths.addAll(Arrays.asList(classPath));
        return this;
    }

    public ClassPathScanner classPath(Collection<String> classPath) {
        this.classPaths.addAll(classPath);
        return this;
    }

    public ClassPathScanner url(URL... urls) {
        this.urls.addAll(Arrays.asList(urls));
        return this;
    }

    public ClassPathScanner excludePackage(String... classPath) {
        this.excludedPackages.addAll(Arrays.asList(classPath));
        return this;
    }

    public ClassPathScanner excludePackage(Collection<String> classPath) {
        this.excludedPackages.addAll(classPath);
        return this;
    }

    public ClassPathScanner classLoader(ClassLoader... classLoaders) {
        this.classLoaders.addAll(Arrays.asList(classLoaders));
        return this;
    }

    public ClassPathScanner classLoader(Collection<ClassLoader> classLoaders) {
        this.classLoaders.addAll(classLoaders);
        return this;
    }

    public ClassPathScanner included(ContainerObject... containerObjects) {
        this.included.addAll(Arrays.asList(containerObjects));
        return this;
    }

    public ClassPathScanner included(Collection<ContainerObject> containerObjects) {
        this.included.addAll(containerObjects);
        return this;
    }

    public abstract void scan() throws Exception;

    public abstract CompletableFuture<List<ContainerObject>> getCompletedFuture();

    public abstract Throwable getException();

}
