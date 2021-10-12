package org.fairy.bean.controller;

import org.fairy.bean.BeanContext;
import org.fairy.bean.details.BeanDetails;

// Internal class
public interface BeanController {

    void applyBean(BeanDetails beanDetails) throws Exception;

    void removeBean(BeanDetails beanDetails) throws Exception;

}
