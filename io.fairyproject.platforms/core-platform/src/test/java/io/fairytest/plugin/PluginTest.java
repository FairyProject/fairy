package io.fairytest.plugin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.fairyproject.plugin.PluginDescription;
import io.fairyproject.tests.TestingBase;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStreamReader;

public class PluginTest extends TestingBase {

    @Test
    public void descriptionParser() {
        PluginDescription expected = PluginDescription.builder()
                .name("test")
                .mainClass("io.fairytest.plugin.PluginMock")
                .shadedPackage("io.fairytest.plugin")
                .module(Pair.of("module.command", "0.0.1b1"))
                .build();

        JsonObject jsonObject;
        try {
            jsonObject = new Gson().fromJson(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("fairy.json")), JsonObject.class);
        } catch (Throwable throwable) {
            throw new IllegalArgumentException("Unable to load fairy.json", throwable);
        }

        Assert.assertEquals(expected, new PluginDescription(jsonObject));
    }

}
