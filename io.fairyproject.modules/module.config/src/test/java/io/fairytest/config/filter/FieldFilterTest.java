package io.fairytest.config.filter;

import io.fairyproject.config.filter.FieldFilters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FieldFilterTest {

    @Test
    public void filterDeclaredFieldsOf() throws NoSuchFieldException {
        List<? extends Field> fields = Arrays.asList(
                DummyClassB.class.getDeclaredField("c"),
                DummyClassA.class.getDeclaredField("a")
        );

        final List<? extends Field> result = FieldFilters.DEFAULT.filterDeclaredFieldsOf(DummyClassB.class);
        Assertions.assertEquals(result, fields);
    }

    @Test
    public void filterDeclaredFieldsOfNoNested() throws NoSuchFieldException {
        List<? extends Field> fields = Collections.singletonList(
                DummyClassC.class.getDeclaredField("c")
        );

        final List<? extends Field> result = FieldFilters.DEFAULT.filterDeclaredFieldsOf(DummyClassC.class);
        Assertions.assertEquals(result, fields);
    }

}
