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

package org.fairy.bean.details;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.fairy.bean.*;
import org.fairy.plugin.AbstractPlugin;
import org.fairy.util.Utility;
import org.fairy.util.terminable.composite.CompositeClosingException;
import org.fairy.util.terminable.composite.CompositeTerminable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class GenericBeanDetails implements BeanDetails {

    private static final Class<? extends Annotation>[] ANNOTATIONS = new Class[] {
            PreInitialize.class, PostInitialize.class,
            PreDestroy.class, PostDestroy.class,
            ShouldInitialize.class
    };

    private String name;

    private ActivationStage stage;
    private Map<Class<? extends Annotation>, String> disallowAnnotations;
    private Map<Class<? extends Annotation>, Collection<Method>> annotatedMethods;

    private AbstractPlugin plugin;

    @Nullable
    private Object instance;
    private Class<?> type;

    private Set<String> children;
    private Map<String, String> tags;

    public GenericBeanDetails(Object instance) {
        this(instance.getClass(), instance, "dummy");
    }

    public GenericBeanDetails(Object instance, Service service) {
        this(instance.getClass(), instance, service.name());
    }

    public GenericBeanDetails(Class<?> type, String name) {
        this.type = type;
        this.name = name;
        this.stage = ActivationStage.NOT_LOADED;
        this.tags = new ConcurrentHashMap<>(0);
        this.children = new HashSet<>();
    }

    public GenericBeanDetails(Class<?> type, @Nullable Object instance, String name) {
        this(type, name);
        this.instance = instance;
        this.loadAnnotations();
    }

    private final CompositeTerminable compositeTerminable = CompositeTerminable.create();

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
                    if (parameterCount != 1 || !BeanDetails.class.isAssignableFrom(method.getParameterTypes()[0])) {
                        throw new IllegalArgumentException("The method " + method.toString() + " used annotation " + annotation.getSimpleName() + " but doesn't have matches parameters! you can only use either no parameter or one parameter with ServerData type on annotated " + annotation.getSimpleName() + "!");
                    }
                }

                if (annotation == ShouldInitialize.class && method.getReturnType() != boolean.class) {
                    throw new IllegalArgumentException("The method " + method.toString() + " used annotation " + annotation.getSimpleName() + " but doesn't have matches return type! you can only use boolean as return type on annotated " + annotation.getSimpleName() + "!");
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
    public void call(Class<? extends Annotation> annotation) throws InvocationTargetException, IllegalAccessException {
        if (instance == null) {
            throw new NullPointerException("The Instance of bean details for " + this.type.getName() + " is null.");
        }

        if (this.annotatedMethods.containsKey(annotation)) {
            for (Method method : this.annotatedMethods.get(annotation)) {
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

        this.changeStage(annotation);
    }

    private void changeStage(Class<? extends Annotation> annotation) {
        if (annotation == PreInitialize.class) {
            this.stage = ActivationStage.PRE_INIT_CALLED;
        } else if (annotation == PostInitialize.class) {
            this.stage = ActivationStage.POST_INIT_CALLED;
        } else if (annotation == PreDestroy.class) {
            this.stage = ActivationStage.PRE_DESTROY_CALLED;
        } else if (annotation == PostDestroy.class) {
            this.stage = ActivationStage.POST_DESTROY_CALLED;
        }
    }

    @Override
    public boolean isStage(ActivationStage stage) {
        return this.stage == stage;
    }

    @Override
    public boolean isActivated() {
        return this.stage == ActivationStage.PRE_INIT_CALLED || this.stage == ActivationStage.POST_INIT_CALLED;
    }

    @Override
    public boolean isDestroyed() {
        return this.stage == ActivationStage.PRE_DESTROY_CALLED || this.stage == ActivationStage.POST_DESTROY_CALLED;
    }

    @Override
    public boolean hasDependencies() {
        return false;
    }

    @Override
    public List<String> getDependencies(ServiceDependencyType type) {
        return Collections.emptyList();
    }

    @Override
    public Set<Map.Entry<ServiceDependencyType, List<String>>> getDependencyEntries() {
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
    public void bindWith(AbstractPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public AbstractPlugin getBindPlugin() {
        return this.plugin;
    }

    public Set<String> getChildren() {
        return Collections.unmodifiableSet(this.children);
    }

    @Override
    public void addChildren(String children) {
        this.children.add(children);
    }

    @Override
    public void removeChildren(String children) {
        this.children.remove(children);
    }

    @Override
    public boolean isBind() {
        return this.plugin != null;
    }

}
