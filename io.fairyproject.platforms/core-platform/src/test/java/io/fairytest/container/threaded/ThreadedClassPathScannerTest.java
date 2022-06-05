package io.fairytest.container.threaded;

import io.fairyproject.container.controller.ContainerController;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.container.scanner.ThreadedClassPathScanner;
import io.fairyproject.tests.base.JUnitJupiterBase;
import io.fairytest.container.ContainerObjMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class ThreadedClassPathScannerTest extends JUnitJupiterBase {

    @Test
    @Disabled // Test is random and does not necessarily create multiple threads
    public void applyControllersShouldBeParallel() {
        if (Runtime.getRuntime().availableProcessors() < 2) {
            // Skip if it's single core?
            return;
        }

        final ThreadedClassPathScanner classPathScanner = new ThreadedClassPathScanner();
        List<Thread> threads = new ArrayList<>();
        int objects = 20;

        for (int i = 0; i < objects; i++) {
            classPathScanner.getContainerObjList().add(new ContainerObjMock());
        }

        classPathScanner.applyControllers(new ContainerController[] {
                new ContainerController() {
                    @Override
                    public void applyContainerObject(ContainerObj containerObj) {
                        threads.add(Thread.currentThread());
                    }

                    @Override
                    public void removeContainerObject(ContainerObj containerObj) {
                        // Do nothing
                    }
                }
        });

        Assertions.assertTrue(threads.stream().distinct().count() > 1);
    }

}
