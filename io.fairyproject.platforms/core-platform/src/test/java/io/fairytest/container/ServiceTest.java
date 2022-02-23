package io.fairytest.container;

import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.object.ContainerObject;
import io.fairyproject.container.scanner.ClassPathScanner;
import io.fairyproject.tests.TestingBase;
import io.fairyproject.util.exceptionally.ThrowingRunnable;
import io.fairytest.container.annotated.AnnotatedRegistration;
import io.fairytest.container.annotated.BeanInterface;
import io.fairytest.container.annotated.BeanInterfaceImpl;
import io.fairytest.container.service.ServiceMock;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTest extends TestingBase {

    @Test
    public void lifeCycle() {
        final ContainerContext containerContext = ContainerContext.INSTANCE;

        ThrowingRunnable.sneaky(() -> {
            final ClassPathScanner classPathScanner = containerContext.scanClasses()
                    .name("test")
                    .classLoader(ServiceTest.class.getClassLoader())
                    .url(ServiceTest.class.getProtectionDomain().getCodeSource().getLocation())
                    .classPath("io.fairytest.container.service");
            classPathScanner.scan();
            final List<ContainerObject> containerObjects = classPathScanner.getCompletedFuture().join();
            assertEquals(1, containerObjects.size());
            assertEquals(ServiceMock.class, containerObjects.get(0).getInstance().getClass());
        }).run();

        final ServiceMock serviceMock = ServiceMock.STATIC_WIRED;
        assertNotNull(serviceMock);

        assertNotEquals(serviceMock.getConstruct(), -1);
        assertNotEquals(serviceMock.getPreInitialize(), -1);
        assertNotEquals(serviceMock.getPostInitialize(), -1);

        LifeCycle[] lifeCycleOrder = Stream.of(
                        Pair.of(LifeCycle.CONSTRUCT, serviceMock.getConstruct()),
                        Pair.of(LifeCycle.PRE_INITIALIZE, serviceMock.getPreInitialize()),
                        Pair.of(LifeCycle.POST_INITIALIZE, serviceMock.getPostInitialize())
                )
                .sorted(java.util.Map.Entry.comparingByValue())
                .map(Pair::getKey)
                .toArray(LifeCycle[]::new);

        System.out.println(serviceMock.getPreInitialize());
        System.out.println(serviceMock.getPostInitialize());

        assertArrayEquals(new LifeCycle[]{
                LifeCycle.CONSTRUCT,
                LifeCycle.PRE_INITIALIZE,
                LifeCycle.POST_INITIALIZE
        }, lifeCycleOrder);

        assertEquals(-1, serviceMock.getPreDestroy());
        assertEquals(-1, serviceMock.getPostDestroy());

        containerContext.disableObjectUnchecked(ServiceMock.class);

        assertNotEquals(serviceMock.getPreDestroy(), -1);
        assertNotEquals(serviceMock.getPostDestroy(), -1);

        lifeCycleOrder = Stream.of(
                        Pair.of(LifeCycle.PRE_DESTROY, serviceMock.getPreDestroy()),
                        Pair.of(LifeCycle.POST_DESTROY, serviceMock.getPostDestroy())
                )
                .sorted(java.util.Map.Entry.comparingByValue())
                .map(Pair::getKey)
                .toArray(LifeCycle[]::new);

        assertArrayEquals(lifeCycleOrder, new LifeCycle[]{
                LifeCycle.PRE_DESTROY,
                LifeCycle.POST_DESTROY
        });
    }

    @Test
    public void annotatedBeanRegistration() {
        final ContainerContext containerContext = ContainerContext.INSTANCE;

        ThrowingRunnable.unchecked(() -> {
            final ClassPathScanner classPathScanner = containerContext.scanClasses()
                    .name("test")
                    .classLoader(ServiceTest.class.getClassLoader())
                    .url(ServiceTest.class.getProtectionDomain().getCodeSource().getLocation())
                    .classPath("io.fairytest.container.annotated");
            classPathScanner.scan();

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
