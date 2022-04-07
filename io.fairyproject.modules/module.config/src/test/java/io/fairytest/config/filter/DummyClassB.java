package io.fairytest.config.filter;

import io.fairyproject.config.annotation.NestedConfig;

@NestedConfig(DummyClassA.class)
public class DummyClassB extends DummyClassA {

    private String c;

}
