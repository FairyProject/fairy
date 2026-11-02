package io.fairytest.config.yaml;

import io.fairyproject.config.yaml.YamlConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class YamlSharedValueTest {

    @Test
    public void sharedValuesAreSavedWithoutAliases() throws Exception {
        Path file = Files.createTempDirectory("fairy-config").resolve("shared.yml");
        SharedConfig config = new SharedConfig(file);
        List<String> shared = new ArrayList<>(Arrays.asList("a", "b"));
        for (int i = 0; i < 100; i++) {
            config.values.put("key" + i, shared);
        }
        config.save();

        String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        Assertions.assertFalse(text.contains("&id"), "saved with anchors");

        SharedConfig loaded = new SharedConfig(file);
        loaded.load();
        Assertions.assertEquals(100, loaded.values.size());
        Assertions.assertEquals(shared, loaded.values.get("key99"));
    }

    public static final class SharedConfig extends YamlConfiguration {
        private Map<String, List<String>> values = new LinkedHashMap<>();

        SharedConfig(Path path) {
            super(path);
        }
    }
}
