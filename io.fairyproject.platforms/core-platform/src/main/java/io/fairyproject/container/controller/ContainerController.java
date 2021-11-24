package io.fairyproject.container.controller;

import io.fairyproject.container.object.ContainerObject;

// Internal class
public interface ContainerController {

    void applyBean(ContainerObject containerObject) throws Exception;

    void removeBean(ContainerObject containerObject) throws Exception;

}
