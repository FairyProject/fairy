package io.fairytest.bean.annotated;

import io.fairyproject.container.Autowired;
import io.fairyproject.container.Register;

public class AnnotatedRegistration {

    @Autowired
    public static BeanInterface INTERFACE;

    @Register(as = BeanInterface.class)
    public static BeanInterface beanInterface() {
        return new BeanInterfaceImpl();
    }

}
