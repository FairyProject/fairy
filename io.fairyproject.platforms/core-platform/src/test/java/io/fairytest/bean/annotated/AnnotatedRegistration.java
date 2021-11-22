package io.fairytest.bean.annotated;

import io.fairyproject.bean.Autowired;
import io.fairyproject.bean.Bean;

public class AnnotatedRegistration {

    @Autowired
    public static BeanInterface INTERFACE;

    @Bean(as = BeanInterface.class)
    public static BeanInterface beanInterface() {
        return new BeanInterfaceImpl();
    }

}
