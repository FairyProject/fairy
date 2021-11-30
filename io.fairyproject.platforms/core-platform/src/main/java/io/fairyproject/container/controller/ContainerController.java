package io.fairyproject.container.controller;

import io.fairyproject.container.object.ContainerObject;

// Internal class
public interface ContainerController {

    void applyContainerObject(ContainerObject containerObject) throws Exception;

    void removeContainerObject(ContainerObject containerObject) throws Exception;

}
