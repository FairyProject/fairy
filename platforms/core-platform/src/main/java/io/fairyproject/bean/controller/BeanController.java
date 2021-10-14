package io.fairyproject.bean.controller;

import io.fairyproject.bean.details.BeanDetails;

// Internal class
public interface BeanController {

    void applyBean(BeanDetails beanDetails) throws Exception;

    void removeBean(BeanDetails beanDetails) throws Exception;

}
