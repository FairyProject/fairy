package io.fairyproject.container.scanner;

import io.fairyproject.Debug;
import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.node.ContainerNode;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.util.SimpleTiming;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class ClassPathScanner {

    protected String prefix = "";
    protected String scanName;

    protected ContainerNode node;

    protected final List<String> classPaths = new ArrayList<>();
    protected final List<String> excludedPackages = new ArrayList<>();
    protected final List<URL> urls = new ArrayList<>();
    protected final List<ClassLoader> classLoaders = new ArrayList<>();

    protected final List<ContainerObj> included = new ArrayList<>();

    public static void log(String msg, Object... replacement) {
        Debug.log(msg, replacement);
    }

    public static SimpleTiming logTiming(String msg) {
        return Debug.logTiming(msg);
    }

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

    public ClassPathScanner included(ContainerObj... containerObjs) {
        this.included.addAll(Arrays.asList(containerObjs));
        return this;
    }

    public ClassPathScanner included(Collection<ContainerObj> containerObjs) {
        this.included.addAll(containerObjs);
        return this;
    }

    public ClassPathScanner node(ContainerNode node) {
        this.node = node;
        return this;
    }

    public void scanBlocking() throws Exception {
        this.scan();
    }

    public abstract void scan() throws Exception;

    public abstract CompletableFuture<List<ContainerObj>> getCompletedFuture();

    public abstract Throwable getException();

}
