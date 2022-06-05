package io.fairyproject.container.controller;

import io.fairyproject.container.object.ContainerObj;
import io.github.classgraph.ScanResult;

// Internal class
public interface ContainerController {

    default void init(ScanResult scanResult) {}

    void applyContainerObject(ContainerObj containerObj) throws Exception;

    void removeContainerObject(ContainerObj containerObj) throws Exception;

}
