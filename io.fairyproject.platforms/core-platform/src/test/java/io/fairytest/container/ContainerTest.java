package io.fairytest.container;

import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.object.ContainerObject;
import io.fairyproject.container.scanner.ClassPathScanner;
import io.fairyproject.tests.base.JUnitJupiterBase;
import io.fairyproject.util.exceptionally.ThrowingRunnable;
import io.fairytest.container.annotated.AnnotatedRegistration;
import io.fairytest.container.annotated.BeanInterface;
import io.fairytest.container.annotated.BeanInterfaceImpl;
import io.fairytest.container.service.ServiceMock;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ContainerTest extends JUnitJupiterBase {

    @Test
    public void syncLifeCycle() {
        final ContainerContext containerContext = ContainerContext.get();
        final Thread mainThread = Thread.currentThread();

        ThrowingRunnable.sneaky(() -> {
            final ClassPathScanner classPathScanner = containerContext.scanClasses()
                    .name("test")
                    .classLoader(ContainerTest.class.getClassLoader())
                    .url(ContainerTest.class.getProtectionDomain().getCodeSource().getLocation())
                    .classPath("io.fairytest.container.service");
            classPathScanner.scanBlocking();
            final List<ContainerObject> containerObjects = classPathScanner.getCompletedFuture().join();
            assertEquals(1, containerObjects.size());
            assertEquals(ServiceMock.class, containerObjects.get(0).getInstance().getClass());
        }).run();

        final ServiceMock serviceMock = ServiceMock.STATIC_WIRED;
        assertNotNull(serviceMock);

        assertNotEquals(-1, serviceMock.getConstructMs());
        assertNotEquals(-1, serviceMock.getPreInitializeMs());
        assertNotEquals(-1, serviceMock.getPostInitializeMs());

        LifeCycle[] lifeCycleOrder = Stream.of(
                        Pair.of(LifeCycle.CONSTRUCT, serviceMock.getConstructMs()),
                        Pair.of(LifeCycle.PRE_INITIALIZE, serviceMock.getPreInitializeMs()),
                        Pair.of(LifeCycle.POST_INITIALIZE, serviceMock.getPostInitializeMs())
                )
                .sorted(java.util.Map.Entry.comparingByValue())
                .map(Pair::getKey)
                .toArray(LifeCycle[]::new);

        assertArrayEquals(new LifeCycle[]{
                LifeCycle.CONSTRUCT,
                LifeCycle.PRE_INITIALIZE,
                LifeCycle.POST_INITIALIZE
        }, lifeCycleOrder);

        assertEquals(mainThread, serviceMock.getConstructThread());
        assertEquals(mainThread, serviceMock.getPreInitializeThread());
        assertEquals(mainThread, serviceMock.getPostInitializeThread());

        assertEquals(-1, serviceMock.getPreDestroyMs());
        assertEquals(-1, serviceMock.getPostDestroyMs());

        containerContext.disableObjectUnchecked(ServiceMock.class);

        assertNotEquals(-1, serviceMock.getPreDestroyMs());
        assertNotEquals(-1, serviceMock.getPostDestroyMs());

        lifeCycleOrder = Stream.of(
                        Pair.of(LifeCycle.PRE_DESTROY, serviceMock.getPreDestroyMs()),
                        Pair.of(LifeCycle.POST_DESTROY, serviceMock.getPostDestroyMs())
                )
                .sorted(java.util.Map.Entry.comparingByValue())
                .map(Pair::getKey)
                .toArray(LifeCycle[]::new);

        assertArrayEquals(lifeCycleOrder, new LifeCycle[]{
                LifeCycle.PRE_DESTROY,
                LifeCycle.POST_DESTROY
        });

        assertEquals(mainThread, serviceMock.getPreDestroyThread());
        assertEquals(mainThread, serviceMock.getPostDestroyThread());
    }

    @Test
    public void annotatedBeanRegistration() {
        final ContainerContext containerContext = ContainerContext.get();

        ThrowingRunnable.sneaky(() -> {
            final ClassPathScanner classPathScanner = containerContext.scanClasses()
                    .name("test")
                    .classLoader(ContainerTest.class.getClassLoader())
                    .url(ContainerTest.class.getProtectionDomain().getCodeSource().getLocation())
                    .classPath("io.fairytest.container.annotated");
            classPathScanner.scanBlocking();

            final List<ContainerObject> beanDetails = classPathScanner.getCompletedFuture().join();
            assertEquals(1, beanDetails.size());
            assertEquals(BeanInterface.class, beanDetails.get(0).getType());
            assertEquals(BeanInterfaceImpl.class, beanDetails.get(0).getInstance().getClass());
        }).run();

        assertNotNull(containerContext.getContainerObject(BeanInterface.class));
        assertNotNull(AnnotatedRegistration.INTERFACE);
    }

    private enum LifeCycle {

        CONSTRUCT,
        PRE_INITIALIZE,
        POST_INITIALIZE,
        PRE_DESTROY,
        POST_DESTROY

    }

}
