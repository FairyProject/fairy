package io.fairytest.mc;

import io.fairyproject.locale.util.YamlResourceBundle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class YamlResourceBundleTest {

    @Test
    public void readStrings() {
        YamlResourceBundle resourceBundle = new YamlResourceBundle(YamlResourceBundleTest.class.getClassLoader().getResourceAsStream("dummy.yml"));

        Assertions.assertEquals("Hi! Whats up", resourceBundle.getString("a"), "Normal string element");
        Assertions.assertEquals("This is second element", resourceBundle.getString("b"), "Normal string element");
        Assertions.assertEquals("This is list element\nIt suppose to have 2 lines at least", resourceBundle.getString("c"), "List string element");

        Assertions.assertThrows(NullPointerException.class, () -> {
            resourceBundle.getString("d");
        }, "Not existing element");
    }

}
