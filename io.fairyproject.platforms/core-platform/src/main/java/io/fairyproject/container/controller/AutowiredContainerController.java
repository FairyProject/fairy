package io.fairyproject.container.controller;

import io.fairyproject.container.Autowired;
import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.ContainerHolder;
import io.fairyproject.container.controller.node.AutowiredNodeController;
import io.fairyproject.container.controller.node.NodeController;
import io.fairyproject.container.node.ContainerNode;
import io.fairyproject.container.object.ContainerObj;
import io.fairyproject.log.Log;
import io.fairyproject.reflect.Reflect;
import io.fairyproject.util.AccessUtil;
import io.fairyproject.util.exceptionally.ThrowingSupplier;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AutowiredContainerController implements ContainerController {

    public static AutowiredContainerController INSTANCE;

    public AutowiredContainerController() {
        INSTANCE = this;
    }

    @Override
    public NodeController initNode(ContainerNode node) {
        return new AutowiredNodeController(this);
    }

    @Override
    public void applyContainerObject(ContainerObj containerObj) throws Exception {
        Object object = containerObj.instance();
        if (object != null) {
            this.applyObject(object);
        }
    }

    @Override
    public void removeContainerObject(ContainerObj containerObj) {
        // do nothing
    }

    public void applyObject(Object instance) throws ReflectiveOperationException {
        List<Field> fields = new ArrayList<>();

        // make sure we get all fields from superclasses
        Class<?> clazz = instance.getClass();
        while (clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }

        for (Field field : fields) {
            int modifiers = field.getModifiers();
            Autowired annotation = field.getAnnotation(Autowired.class);

            if (annotation == null || Modifier.isStatic(modifiers)) {
                continue;
            }

            if (Modifier.isFinal(modifiers)) {
                throw new IllegalStateException("The field " + field + " is final but marked @Autowired");
            }

            this.applyField(field, instance);
        }
    }

    public void applyField(Field field, Object instance) throws ReflectiveOperationException {
        Class<?> type = field.getType();
        boolean optional = false, beanHolder = false;
        if (type == Optional.class) {
            optional = true;
            type = ThrowingSupplier.sneaky(() -> Reflect.getParameter(field, 0)).get();
            if (type == null) {
                return;
            }
        } else if (type == ContainerHolder.class) {
            beanHolder = true;

            type = ThrowingSupplier.sneaky(() -> Reflect.getParameter(field, 0)).get();
            if (type == null) {
                return;
            }
        }
        Object objectToInject = ContainerContext.get().getContainerObject(type);
        if (optional) {
            objectToInject = Optional.ofNullable(objectToInject);
        } else if (beanHolder) {
            objectToInject = new ContainerHolder<>(objectToInject);
        }

        if (objectToInject != null) {
            AccessUtil.setAccessible(field);
            Reflect.setField(instance, field, objectToInject);
        } else {
            Log.error("The Autowired field " + field + " trying to wired with type " + type.getSimpleName() + " but couldn't find any matching Service! (or not being registered)");
        }
    }
}
