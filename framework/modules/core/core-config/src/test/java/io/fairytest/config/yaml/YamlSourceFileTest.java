package io.fairytest.config.yaml;

import io.fairyproject.config.yaml.YamlConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class YamlSourceFileTest {

    @TempDir
    Path directory;

    @Test
    public void saveIsWrittenAsUtf8() throws IOException {
        Path file = directory.resolve("config.yml");
        DummyConfiguration configuration = new DummyConfiguration(file);
        configuration.setName("泥土 café");
        configuration.save();

        String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        Assertions.assertTrue(content.contains("泥土 café"), content);
    }

    @Test
    public void savedValuesAreLoadedBack() {
        Path file = directory.resolve("config.yml");
        DummyConfiguration saved = new DummyConfiguration(file);
        saved.setName("泥土 café");
        saved.save();

        DummyConfiguration loaded = new DummyConfiguration(file);
        loaded.load();
        Assertions.assertEquals("泥土 café", loaded.getName());
    }

    @Test
    public void saveReplacesExistingFileWithoutLeavingTemporaryFiles() throws IOException {
        Path file = directory.resolve("config.yml");
        DummyConfiguration configuration = new DummyConfiguration(file);
        configuration.save();
        configuration.load();
        configuration.setName("second");
        configuration.save();

        DummyConfiguration loaded = new DummyConfiguration(file);
        loaded.load();
        Assertions.assertEquals("second", loaded.getName());
        Assertions.assertEquals(1, listDirectory().size(), listDirectory().toString());
    }

    @Test
    public void saveCreatesMissingParentDirectories() {
        Path file = directory.resolve("nested").resolve("config.yml");
        new DummyConfiguration(file).save();

        Assertions.assertTrue(Files.isRegularFile(file));
    }

    @Test
    public void saveKeepsFilePermissions() throws IOException {
        assumePosix();
        Path file = directory.resolve("config.yml");
        DummyConfiguration configuration = new DummyConfiguration(file);
        configuration.save();

        for (String permissions : new String[]{"rw-------", "rw-rw-rw-"}) {
            Files.setPosixFilePermissions(file, PosixFilePermissions.fromString(permissions));
            configuration.save();
            Assertions.assertEquals(permissions, PosixFilePermissions.toString(Files.getPosixFilePermissions(file)));
        }
    }

    @Test
    public void saveFollowsSymbolicLink() throws IOException {
        assumePosix();
        Path target = directory.resolve("real").resolve("config.yml");
        new DummyConfiguration(target).save();
        Path link = Files.createSymbolicLink(directory.resolve("link.yml"), directory.relativize(target));

        DummyConfiguration configuration = new DummyConfiguration(link);
        configuration.setName("second");
        configuration.save();

        Assertions.assertTrue(Files.isSymbolicLink(link));
        DummyConfiguration loaded = new DummyConfiguration(target);
        loaded.load();
        Assertions.assertEquals("second", loaded.getName());
    }

    @Test
    public void saveThroughDanglingSymbolicLinkCreatesTarget() throws IOException {
        assumePosix();
        Path target = directory.resolve("config.yml");
        Path link = Files.createSymbolicLink(directory.resolve("link.yml"), target.getFileName());

        new DummyConfiguration(link).save();

        Assertions.assertTrue(Files.isSymbolicLink(link));
        Assertions.assertTrue(Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS));
    }

    @Test
    public void saveFallsBackToWritingInPlaceWhenDirectoryIsNotWritable() throws IOException {
        assumePosix();
        Path locked = Files.createDirectory(directory.resolve("locked"));
        Path file = locked.resolve("config.yml");
        DummyConfiguration configuration = new DummyConfiguration(file);
        configuration.save();

        Files.setPosixFilePermissions(locked, PosixFilePermissions.fromString("r-xr-xr-x"));
        try {
            Assumptions.assumeFalse(Files.isWritable(locked), "root ignores directory permissions");

            configuration.setName("second");
            configuration.save();

            DummyConfiguration loaded = new DummyConfiguration(file);
            loaded.load();
            Assertions.assertEquals("second", loaded.getName());
        } finally {
            Files.setPosixFilePermissions(locked, PosixFilePermissions.fromString("rwxr-xr-x"));
        }
    }

    private void assumePosix() {
        Assumptions.assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"));
    }

    private List<Path> listDirectory() throws IOException {
        try (Stream<Path> files = Files.list(directory)) {
            return files.collect(Collectors.toList());
        }
    }

    public static class DummyConfiguration extends YamlConfiguration {

        private String name = "first";

        public DummyConfiguration(Path path) {
            super(path);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

}
