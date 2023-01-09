package io.fairyproject.container.object.lifecycle.impl.annotation;

import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.container.object.lifecycle.LifeCycleHandler;
import io.fairyproject.util.AccessUtil;
import io.fairyproject.util.ConditionUtils;
import io.fairyproject.util.Utility;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public abstract class LifeCycleAnnotationProcessor implements LifeCycleHandler {

    private static final int PRE_INIT = 0;
    private static final int POST_INIT = 1;
    private static final int PRE_DESTROY = 2;
    private static final int POST_DESTROY = 3;

    private final Class<? extends Annotation>[] annotations = new Class[4];
    private final List<LifeCycleElement>[] processors = new List[4];
    private final ContainerObj containerObj;

    public LifeCycleAnnotationProcessor(ContainerObj containerObj) {
        this.containerObj = containerObj;
    }

    public void setPreInitAnnotation(@Nullable Class<? extends Annotation> annotation) {
        this.annotations[PRE_INIT] = annotation;
    }

    public void setPostInitAnnotation(@Nullable Class<? extends Annotation> annotation) {
        this.annotations[POST_INIT] = annotation;
    }

    public void setPreDestroyAnnotation(@Nullable Class<? extends Annotation> annotation) {
        this.annotations[PRE_DESTROY] = annotation;
    }

    public void setPostDestroyAnnotation(@Nullable Class<? extends Annotation> annotation) {
        this.annotations[POST_DESTROY] = annotation;
    }

    @Override
    public void init() {
        Class<?> aClass;
        Object instance = containerObj.instance();
        if (instance != null)
            // If container object has instanced, prioritize instance's class as the type could just be a contract interface.
            aClass = instance.getClass();
        else
            aClass = this.containerObj.type();

        for (int i = 0; i < this.processors.length; i++) {
            processors[i] = new ArrayList<>();
        }
        for (Class<?> superClass : Utility.getSuperClasses(aClass)) {
            try {
                for (Method declaredMethod : superClass.getDeclaredMethods()) {
                    this.handleMethodInit(declaredMethod);
                }
            } catch (Throwable throwable) {
                IllegalStateException illegalStateException = new IllegalStateException("An error occurs while scanning class " + superClass, throwable);
                illegalStateException.printStackTrace();
                throw illegalStateException;
            }
        }
    }

    @Override
    public CompletableFuture<?> onPreInit() {
        return this.invokeProcessors(PRE_INIT);
    }

    @Override
    public CompletableFuture<?> onPostInit() {
        return this.invokeProcessors(POST_INIT);
    }

    @Override
    public CompletableFuture<?> onPreDestroy() {
        return this.invokeProcessors(PRE_DESTROY);
    }

    @Override
    public CompletableFuture<?> onPostDestroy() {
        return this.invokeProcessors(POST_DESTROY);
    }

    private CompletableFuture<?> invokeProcessors(int index) {
        return this.containerObj.threadingMode().execute(() -> {
            Object instance = this.containerObj.instance();
            ConditionUtils.notNull(instance, "Instance of the container object hasn't yet been constructed.");

            this.processors[index].forEach(processor -> processor.invoke(instance));
        });
    }

    private void handleMethodInit(Method method) {
        for (int index = 0; index < this.annotations.length; index++) {
            final Class<? extends Annotation> annotation = this.annotations[index];
            this.handleAnnotationInit(method, index, annotation);
        }
    }

    private void handleAnnotationInit(Method method, int index, @Nullable Class<? extends Annotation> annotation) {
        // The annotation is not handled in this processor
        if (annotation == null)
            return;

        // The method wasn't annotated by target annotation
        if (!method.isAnnotationPresent(annotation))
            return;

        // The method has extra parameter which is not allowed
        ConditionUtils.is(method.getParameterCount() == 0, String.format("The method %s annotated with %s is not supposed to have parameters", method, annotation));

        final LifeCycleElement processor = new LifeCycleElement(method);
        this.processors[index].add(processor);
    }

    private static class LifeCycleElement {

        private final MethodHandle methodHandle;
        @Getter
        private final String identifier;

        @SneakyThrows
        public LifeCycleElement(Method method) {
            AccessUtil.setAccessible(method);
            this.methodHandle = MethodHandles.lookup().unreflect(method);
            this.identifier = String.format("%s # %s", method.getClass().getName(), method.getName());
        }

        @SneakyThrows

        public void invoke(Object instance) {
            this.methodHandle.invoke(instance);
        }

    }
}
