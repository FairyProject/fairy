package io.fairytest.pojo;

import io.fairyproject.pojo.PojoListener;
import io.fairyproject.pojo.PojoMapper;
import io.fairyproject.pojo.PojoProperty;
import io.fairyproject.tests.base.JUnitJupiterBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class PojoMapperTest extends JUnitJupiterBase {

    @Test
    public void init_dontAcceptAbstractAndInterfaceClass() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> PojoMapper.create(TestAbstract.class).init());
        Assertions.assertThrows(IllegalArgumentException.class, () -> PojoMapper.create(TestInterface.class).init());
    }

    public abstract class TestAbstract {}
    public interface TestInterface {}

    @Test
    public void init_dontAcceptClassWithFinalField() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> PojoMapper.create(TestFinalField.class).init());
    }

    public class TestFinalField {
        private final String a = "";
    }

    @Test
    public void init_allPropertiesShouldBeFound() throws ReflectiveOperationException {
        final PojoMapper<TestProperties> pojoMapper = PojoMapper.create(TestProperties.class);
        pojoMapper.init();

        final Map<String, PojoProperty> properties = pojoMapper.properties();

        Assertions.assertEquals(3, properties.size());
        Assertions.assertTrue(properties.containsKey("a"));
        Assertions.assertTrue(properties.containsKey("b"));
        Assertions.assertTrue(properties.containsKey("c"));
        Assertions.assertNotNull(pojoMapper.getProperty("a"));
        Assertions.assertNotNull(pojoMapper.getProperty("b"));
        Assertions.assertNotNull(pojoMapper.getProperty("c"));
    }

    public static class TestProperties {
        private String a = "";
        private int b = 0;
        private boolean c = true;
        private transient String ignoreThis = "";
    }

    @Test
    public void addListener_shouldAllBeCalled() throws ReflectiveOperationException {
        final boolean[] bools = new boolean[2];
        final PojoMapper<TestProperties> pojoMapper = PojoMapper.create(TestProperties.class);
        pojoMapper.addListener(new PojoListener() {
            @Override
            public void onPropertyAdded(PojoMapper<?> pojoMapper, PojoProperty pojoProperty) {
                bools[0] = true;
            }

            @Override
            public void onMapperInitialized(PojoMapper<?> pojoMapper) {
                bools[1] = true;
            }
        });
        pojoMapper.init();

        for (boolean bool : bools) {
            Assertions.assertTrue(bool);
        }
    }

}
