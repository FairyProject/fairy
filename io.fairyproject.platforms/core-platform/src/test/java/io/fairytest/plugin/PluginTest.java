package io.fairytest.plugin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.fairyproject.plugin.PluginDescription;
import io.fairyproject.tests.base.JUnitJupiterBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;

public class PluginTest extends JUnitJupiterBase {

    @Test
    public void descriptionParser() {
        PluginDescription expected = PluginDescription.builder()
                .name("test")
                .mainClass("io.fairytest.plugin.PluginMock")
                .shadedPackage("io.fairytest.plugin")
                .fairyPackage("io.fairytest.plugin.fairy")
                .build();

        JsonObject jsonObject;
        try {
            jsonObject = new Gson().fromJson(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("fairy.json")), JsonObject.class);
        } catch (Throwable throwable) {
            throw new IllegalArgumentException("Unable to load fairy.json", throwable);
        }

        Assertions.assertEquals(expected, new PluginDescription(jsonObject));
    }

}
