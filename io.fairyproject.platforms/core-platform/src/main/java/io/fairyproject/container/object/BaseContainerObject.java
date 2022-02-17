/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.fairyproject.container.object;

import io.fairyproject.container.*;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.util.Utility;
import io.fairyproject.util.terminable.composite.CompositeClosingException;
import io.fairyproject.util.terminable.composite.CompositeTerminable;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class BaseContainerObject implements ContainerObject {

    @Autowired
    private static ContainerContext BEAN_CONTEXT;

    @SuppressWarnings("unchecked")
    private static final Class<? extends Annotation>[] ANNOTATIONS = new Class[] {
            PreInitialize.class, PostInitialize.class,
            PreDestroy.class, PostDestroy.class,
            ShouldInitialize.class
    };

    private LifeCycle lifeCycle;
    private Map<Class<? extends Annotation>, String> disallowAnnotations;
    private Map<Class<? extends Annotation>, Collection<Method>> annotatedMethods;
    private ThreadingMode.Mode threadingMode;
    private final CompositeTerminable compositeTerminable = CompositeTerminable.create();

    private Plugin plugin;

    @Nullable
    private Object instance;
    private Class<?> type;

    private Set<Class<?>> children;
    private Map<String, String> tags;

    private boolean closed;

    public BaseContainerObject(Object instance) {
        this(instance.getClass(), instance);
    }

    public BaseContainerObject(Class<?> type) {
        this.type = type;
        this.lifeCycle = LifeCycle.NONE;
        this.tags = new ConcurrentHashMap<>(0);
        this.children = new HashSet<>();
    }

    public BaseContainerObject(Class<?> type, @Nullable Object instance) {
        this(type);
        this.instance = instance;
        this.loadAnnotations();
    }

    public ThreadingMode.Mode getThreadingMode() {
        return this.threadingMode == null ? ThreadingMode.Mode.SYNC : this.threadingMode;
    }

    @NotNull
    @Override
    public <T extends AutoCloseable> T bind(@NotNull T terminable) {
        return this.compositeTerminable.bind(terminable);
    }

    @Override
    public void onDisable() {
        CompositeClosingException ex = this.compositeTerminable.closeSilently();
        if (ex != null) {
            ex.printAllStackTraces();
        }
    }

    @SneakyThrows
    public void loadAnnotations() {
        this.annotatedMethods = new HashMap<>();
        this.disallowAnnotations = new HashMap<>();

        this.loadAnnotations(Utility.getSuperAndInterfaces(this.type));
    }

    public void loadAnnotations(Collection<Class<?>> superClasses) {
        for (Class<?> type : superClasses) {
            DisallowAnnotation disallowAnnotation = type.getAnnotation(DisallowAnnotation.class);
            if (disallowAnnotation != null) {
                for (Class<? extends Annotation> annotation : disallowAnnotation.value()) {
                    this.disallowAnnotations.put(annotation, type.getName());
                }
            }

            ThreadingMode threadingMode = type.getAnnotation(ThreadingMode.class);
            if (threadingMode != null) {
                this.threadingMode = threadingMode.value();
            }
        }

        for (Class<?> type : superClasses) {
            if (type.isInterface()) {
                continue;
            }

            for (Method method : type.getDeclaredMethods()) {
                this.loadMethod(method);
            }
        }
    }

    public void loadMethod(Method method) {
        for (Class<? extends Annotation> annotation : ANNOTATIONS) {
            if (method.getAnnotation(annotation) != null) {
                if (this.disallowAnnotations.containsKey(annotation)) {
                    String className = this.disallowAnnotations.get(annotation);
                    throw new IllegalArgumentException("The annotation " + annotation.getSimpleName() + " is disallowed by class " + className + ", But it used in method " + method.toString());
                }

                int parameterCount = method.getParameterCount();
                if (parameterCount > 0) {
                    if (parameterCount != 1 || !ContainerObject.class.isAssignableFrom(method.getParameterTypes()[0])) {
                        throw new IllegalArgumentException("The method " + method + " used annotation " + annotation.getSimpleName() + " but doesn't have matches parameters! you can only use either no parameter or one parameter with ServerData type on annotated " + annotation.getSimpleName() + "!");
                    }
                }

                if (annotation == ShouldInitialize.class && method.getReturnType() != boolean.class) {
                    throw new IllegalArgumentException("The method " + method + " used annotation " + annotation.getSimpleName() + " but doesn't have matches return type! you can only use boolean as return type on annotated " + annotation.getSimpleName() + "!");
                }
                method.setAccessible(true);

                if (this.annotatedMethods.containsKey(annotation)) {
                    this.annotatedMethods.get(annotation).add(method);
                } else {
                    List<Method> methods = new LinkedList<>();
                    methods.add(method);

                    this.annotatedMethods.put(annotation, methods);
                }
                break;
            }
        }
    }

    @Override
    public boolean shouldInitialize() throws InvocationTargetException, IllegalAccessException  {
        if (this.annotatedMethods == null) {
            return true;
        }

        if (instance == null) {
            throw new NullPointerException("The Instance of bean details for " + this.type.getName() + " is null.");
        }

        if (this.annotatedMethods.containsKey(ShouldInitialize.class)) {
            for (Method method : this.annotatedMethods.get(ShouldInitialize.class)) {
                boolean result;

                if (method.getParameterCount() == 1) {
                    result = (boolean) method.invoke(instance, this);
                } else {
                    result = (boolean) method.invoke(instance);
                }

                if (!result) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public CompletableFuture<?> lifeCycle(@NotNull LifeCycle lifeCycle) {
        this.lifeCycle = lifeCycle;
        switch (lifeCycle) {
            case PRE_INIT:
                return this.call(PreInitialize.class);
            case POST_INIT:
                return this.call(PostInitialize.class);
            case PRE_DESTROY:
                return this.call(PreDestroy.class);
            case POST_DESTROY:
                return this.call(PostDestroy.class);
        }
        return null;
    }

    private CompletableFuture<?> call(Class<? extends Annotation> annotation) {
        if (instance == null) {
            throw new NullPointerException("The Instance of bean details for " + this.type.getName() + " is null.");
        }

        switch (this.getThreadingMode()) {
            case ASYNC:
                return CompletableFuture.runAsync(() -> this.callSync(annotation), ContainerContext.EXECUTOR);
            default:
            case SYNC:
                this.callSync(annotation);
                return CompletableFuture.completedFuture(null);
        }
    }

    private void callSync(Class<? extends Annotation> annotation) {
        for (Method method : this.annotatedMethods.getOrDefault(annotation, Collections.emptyList())) {
            try {
                if (method.getParameterCount() == 1) {
                    method.invoke(instance, this);
                } else {
                    method.invoke(instance);
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    @Override
    public boolean isLifeCycle(LifeCycle lifeCycle) {
        return this.lifeCycle == lifeCycle;
    }

    @Override
    public boolean isActivated() {
        return this.lifeCycle == LifeCycle.PRE_INIT || this.lifeCycle == LifeCycle.POST_INIT;
    }

    @Override
    public boolean isDestroyed() {
        return this.lifeCycle == LifeCycle.PRE_DESTROY || this.lifeCycle == LifeCycle.POST_DESTROY;
    }

    @Override
    public boolean hasDependencies() {
        return false;
    }

    @Override
    public List<Class<?>> getDependencies(ServiceDependencyType type) {
        return Collections.emptyList();
    }

    @Override
    public Set<Map.Entry<ServiceDependencyType, List<Class<?>>>> getDependencyEntries() {
        return Collections.emptySet();
    }

    @Override
    @Nullable
    public String getTag(String key) {
        return this.tags.getOrDefault(key, null);
    }

    @Override
    public boolean hasTag(String key) {
        return this.tags.containsKey(key);
    }

    @Override
    public void addTag(String key, String value) {
        this.tags.put(key, value);
    }

    @Override
    public void bindWith(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Plugin getBindPlugin() {
        return this.plugin;
    }

    public Set<Class<?>> getChildren() {
        return Collections.unmodifiableSet(this.children);
    }

    @Override
    public void addChildren(Class<?> children) {
        this.children.add(children);
    }

    @Override
    public void removeChildren(Class<?> children) {
        this.children.remove(children);
    }

    @Override
    public boolean isBind() {
        return this.plugin != null;
    }

    @Override
    public void close() throws Exception {
        if (this.isClosed()) {
            return;
        }
        this.closed = true;

        this.onDisable();
        BEAN_CONTEXT.unregisterObject(this);
    }

    @Override
    public String toString() {
        return this.type.getName();
    }
}
