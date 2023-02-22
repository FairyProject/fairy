package io.fairyproject.container.controller;

import io.fairyproject.container.controller.node.NodeController;
import io.fairyproject.container.controller.node.NodeControllerNoOp;
import io.fairyproject.container.node.ContainerNode;
import io.fairyproject.container.object.ContainerObj;

// Internal class
public interface ContainerController {

    default NodeController initNode(ContainerNode node) {
        return NodeControllerNoOp.INSTANCE;
    }

    void applyContainerObject(ContainerObj containerObj) throws Exception;

    void removeContainerObject(ContainerObj containerObj) throws Exception;

}
