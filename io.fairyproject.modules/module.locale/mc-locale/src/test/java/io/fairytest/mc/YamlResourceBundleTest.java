package io.fairytest.mc;

import io.fairyproject.Fairy;
import io.fairyproject.locale.util.YamlResourceBundle;
import io.fairyproject.tests.base.JUnitJupiterBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class YamlResourceBundleTest extends JUnitJupiterBase {

    @Test
    public void readStrings() {
        YamlResourceBundle resourceBundle = new YamlResourceBundle(Fairy.getPlatform().getResource("dummy.yml"));

        Assertions.assertEquals("Hi! Whats up", resourceBundle.getString("a"), "Normal string element");
        Assertions.assertEquals("This is second element", resourceBundle.getString("b"), "Normal string element");
        Assertions.assertEquals("This is list element\nIt suppose to have 2 lines at least", resourceBundle.getString("c"), "List string element");

        Assertions.assertThrows(NullPointerException.class, () -> {
            resourceBundle.getString("d");
        }, "Not existing element");
    }

}
