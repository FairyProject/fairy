package io.fairytest.container;

import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.node.ContainerNode;
import io.fairyproject.container.node.loader.ContainerNodeLoader;
import io.fairyproject.container.node.scanner.ContainerNodeClassScanner;
import io.fairyproject.tests.base.JUnitJupiterBase;
import io.fairyproject.util.entry.Entry;
import io.fairyproject.util.exceptionally.ThrowingRunnable;
import io.fairytest.container.service.ServiceMock;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ContainerTest extends JUnitJupiterBase {

    @Test
    public void syncLifeCycle() {
        final ContainerContext context = ContainerContext.get();
        final Thread mainThread = Thread.currentThread();

        ThrowingRunnable.sneaky(() -> {
            ContainerNode node = ContainerNode.create("test");
            ContainerNodeClassScanner classScanner = new ContainerNodeClassScanner(context, "test", node);
            classScanner.getClassLoaders().add(ContainerTest.class.getClassLoader());
            classScanner.getUrls().add(ContainerTest.class.getProtectionDomain().getCodeSource().getLocation());
            classScanner.getClassPaths().add("io.fairytest.container.service");
            classScanner.scan();

            new ContainerNodeLoader(context, node).load();

            assertEquals(1, node.all().size());
            assertEquals(ServiceMock.class, node.all().iterator().next().type());
        }).run();

        final ServiceMock serviceMock = ServiceMock.STATIC_WIRED;
        assertNotNull(serviceMock);

        assertNotEquals(-1, serviceMock.getConstructMs());
        assertNotEquals(-1, serviceMock.getPreInitializeMs());
        assertNotEquals(-1, serviceMock.getPostInitializeMs());

        LifeCycle[] lifeCycleOrder = Stream.of(
                        new Entry<>(LifeCycle.CONSTRUCT, serviceMock.getConstructMs()),
                        new Entry<>(LifeCycle.PRE_INITIALIZE, serviceMock.getPreInitializeMs()),
                        new Entry<>(LifeCycle.POST_INITIALIZE, serviceMock.getPostInitializeMs())
                )
                .sorted(Comparator.comparing(Entry::getValue))
                .map(Entry::getKey)
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

//        containerContext.disableObjectUnchecked(ServiceMock.class);
//
//        assertNotEquals(-1, serviceMock.getPreDestroyMs());
//        assertNotEquals(-1, serviceMock.getPostDestroyMs());
//
//        lifeCycleOrder = Stream.of(
//                        Pair.of(LifeCycle.PRE_DESTROY, serviceMock.getPreDestroyMs()),
//                        Pair.of(LifeCycle.POST_DESTROY, serviceMock.getPostDestroyMs())
//                )
//                .sorted(java.util.Map.Entry.comparingByValue())
//                .map(Pair::getKey)
//                .toArray(LifeCycle[]::new);
//
//        assertArrayEquals(lifeCycleOrder, new LifeCycle[]{
//                LifeCycle.PRE_DESTROY,
//                LifeCycle.POST_DESTROY
//        });
//
//        assertEquals(mainThread, serviceMock.getPreDestroyThread());
//        assertEquals(mainThread, serviceMock.getPostDestroyThread());
    }

    private enum LifeCycle {

        CONSTRUCT,
        PRE_INITIALIZE,
        POST_INITIALIZE,
        PRE_DESTROY,
        POST_DESTROY

    }

}
