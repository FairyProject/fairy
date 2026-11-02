package io.fairyproject.container;

import io.fairyproject.container.node.ContainerNode;
import io.fairyproject.container.node.scanner.ContainerNodeClassScanner;
import io.fairytest.container.destroy.DestroyTrackingComponent;
import io.fairytest.container.destroy.ErrorDestroyComponent;
import io.fairytest.container.destroy.FailingDestroyComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ContainerNodeDestroyerTest {

    private ContainerContext context;
    private ContainerNode node;

    @BeforeEach
    public void setUp() {
        this.context = new ContainerContext();
        this.node = ContainerNode.create("test", context.containerObjectBinder());

        ContainerNodeClassScanner classScanner = new ContainerNodeClassScanner(context, context.containerObjectBinder(), "test", node);
        classScanner.getClassLoaders().add(ContainerNodeDestroyerTest.class.getClassLoader());
        classScanner.getUrls().add(ContainerNodeDestroyerTest.class.getProtectionDomain().getCodeSource().getLocation());
        classScanner.getClassPaths().add("io.fairytest.container.destroy");
        classScanner.scan();

        context.loadContainerNode(node);
    }

    @Test
    public void failingComponentDoesNotStopOthersFromBeingDestroyed() {
        DestroyTrackingComponent tracking = (DestroyTrackingComponent) context.singletonObjectRegistry().getSingleton(DestroyTrackingComponent.class);
        assertNotNull(tracking);

        assertDoesNotThrow(() -> context.nodeDestroyer().destroy(node));

        assertTrue(tracking.isPreDestroyed());
        assertTrue(tracking.isPostDestroyed());
    }

    @Test
    public void failingComponentIsStillRemoved() {
        context.nodeDestroyer().destroy(node);

        assertFalse(context.containerObjectBinder().isBound(FailingDestroyComponent.class));
        assertFalse(context.singletonObjectRegistry().containsSingleton(FailingDestroyComponent.class));
        assertFalse(context.containerObjectBinder().isBound(ErrorDestroyComponent.class));
        assertFalse(context.singletonObjectRegistry().containsSingleton(ErrorDestroyComponent.class));
        assertFalse(context.singletonObjectRegistry().containsSingleton(DestroyTrackingComponent.class));
    }

}
