package io.fairytest.config.yaml;

import io.fairyproject.config.yaml.YamlConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class YamlConfigurationConcurrencyTest {

    private static final int CONFIGURATIONS = 8;
    private static final int SAVES = 25;

    @TempDir
    Path directory;

    @Test
    public void configurationsCanBeSavedConcurrently() throws Exception {
        List<DummyConfiguration> configurations = new ArrayList<>();
        for (int i = 0; i < CONFIGURATIONS; i++) {
            configurations.add(new DummyConfiguration(directory.resolve("config-" + i + ".yml")));
        }

        ExecutorService executor = Executors.newFixedThreadPool(CONFIGURATIONS);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (DummyConfiguration configuration : configurations) {
                futures.add(executor.submit(() -> saveRepeatedly(configuration)));
            }
            for (Future<?> future : futures) {
                future.get(30, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
        }

        for (DummyConfiguration configuration : configurations) {
            Assertions.assertEquals(configuration.expectedNames(), configuration.loadEntries().get("names"));
        }
    }

    private static void saveRepeatedly(DummyConfiguration configuration) {
        for (int i = 0; i < SAVES; i++) {
            configuration.save();
        }
    }

    public static class DummyConfiguration extends YamlConfiguration {

        private List<String> names = new ArrayList<>();
        private Map<String, Integer> values = new LinkedHashMap<>();

        public DummyConfiguration(Path path) {
            super(path);
            for (int i = 0; i < 200; i++) {
                names.add("name-" + i);
                values.put("key-" + i, i);
            }
        }

        List<String> expectedNames() {
            return names;
        }

    }

}
