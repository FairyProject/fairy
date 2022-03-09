package io.fairytest.config.filter;

import com.google.common.collect.ImmutableList;
import io.fairyproject.config.filter.FieldFilters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

public class FieldFilterTest {

    @Test
    public void filterDeclaredFieldsOf() throws NoSuchFieldException {
        List<? extends Field> fields = ImmutableList.of(
                DummyClassB.class.getDeclaredField("c"),
                DummyClassA.class.getDeclaredField("a")
        );

        final List<? extends Field> result = FieldFilters.DEFAULT.filterDeclaredFieldsOf(DummyClassB.class);
        Assertions.assertEquals(result, fields);
    }

    @Test
    public void filterDeclaredFieldsOfNoNested() throws NoSuchFieldException {
        List<? extends Field> fields = ImmutableList.of(
                DummyClassC.class.getDeclaredField("c")
        );

        final List<? extends Field> result = FieldFilters.DEFAULT.filterDeclaredFieldsOf(DummyClassC.class);
        Assertions.assertEquals(result, fields);
    }

}
