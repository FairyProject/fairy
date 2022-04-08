package io.fairyproject.container.controller;

import io.fairyproject.container.ContainerContext;
import io.fairyproject.container.ContainerHolder;
import io.fairyproject.reflect.Reflect;
import io.fairyproject.util.exceptionally.ThrowingSupplier;
import org.apache.logging.log4j.LogManager;
import io.fairyproject.container.Autowired;
import io.fairyproject.container.object.ContainerObject;
import io.fairyproject.util.AccessUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Optional;

public class AutowiredContainerController implements ContainerController {

    public static AutowiredContainerController INSTANCE;

    public AutowiredContainerController() {
        INSTANCE = this;
    }

    @Override
    public void applyContainerObject(ContainerObject containerObject) throws Exception {
        Object object = containerObject.getInstance();
        if (object != null) {
            this.applyObject(object);
        }
    }

    @Override
    public void removeContainerObject(ContainerObject containerObject) {

    }

    public void applyObject(Object instance) throws ReflectiveOperationException {
        Field[] fields = instance.getClass().getDeclaredFields();

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
        Object objectToInject = ContainerContext.INSTANCE.getContainerObject(type);
        if (optional) {
            objectToInject = Optional.ofNullable(objectToInject);
        } else if (beanHolder) {
            objectToInject = new ContainerHolder<>(objectToInject);
        }

        if (objectToInject != null) {
            AccessUtil.setAccessible(field);
            Reflect.setField(instance, field, objectToInject);
        } else {
            LogManager.getLogger(AutowiredContainerController.class).error("The Autowired field " + field + " trying to wired with type " + type.getSimpleName() + " but couldn't find any matching Service! (or not being registered)");
        }
    }
}
