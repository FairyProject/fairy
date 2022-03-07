package io.fairytest;

import io.fairyproject.shared.FairyVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.TreeSet;

public class FairyVersionTest {

    @Test
    public void checkMajor() {
        FairyVersion lowerVersion = FairyVersion.parse("5.0.0b1");
        FairyVersion newerVersion = FairyVersion.parse("6.0.0b1");

        Assertions.assertTrue(newerVersion.isAbove(lowerVersion));
    }

    @Test
    public void checkMinor() {
        FairyVersion lowerVersion = FairyVersion.parse("5.0.0b1");
        FairyVersion newerVersion = FairyVersion.parse("5.1.0b1");

        Assertions.assertTrue(newerVersion.isAbove(lowerVersion));
    }

    @Test
    public void checkRevision() {
        FairyVersion lowerVersion = FairyVersion.parse("5.0.0b1");
        FairyVersion newerVersion = FairyVersion.parse("5.0.1b1");

        Assertions.assertTrue(newerVersion.isAbove(lowerVersion));
    }

    @Test
    public void checkBuild() {
        FairyVersion lowerVersion = FairyVersion.parse("5.0.0b1");
        FairyVersion newerVersion = FairyVersion.parse("5.0.0b2");

        Assertions.assertTrue(newerVersion.isAbove(lowerVersion));
    }

    @Test
    public void checkTag() {
        FairyVersion lowerVersion = FairyVersion.parse("5.0.0b1-SNAPSHOT");
        FairyVersion newerVersion = FairyVersion.parse("5.0.0b1");

        Assertions.assertTrue(newerVersion.isAbove(lowerVersion));
    }

    @Test
    public void checkToString() {
        FairyVersion version = FairyVersion.parse("5.0.0b1");

        Assertions.assertEquals(version.toString(), "5.0.0b1");
    }

    @Test
    public void checkToStringWithTag() {
        FairyVersion version = FairyVersion.parse("5.0.0b1-SNAPSHOT");

        Assertions.assertEquals(version.toString(), "5.0.0b1-SNAPSHOT");
    }

    @Test
    public void checkTreeMap() {
        FairyVersion lowerVersion = FairyVersion.parse("5.0.0b1");
        FairyVersion newerVersion = FairyVersion.parse("5.0.1b1");

        TreeSet<FairyVersion> treeSet = new TreeSet<>();
        treeSet.add(newerVersion);
        treeSet.add(lowerVersion);

        Assertions.assertEquals(newerVersion, treeSet.last());
    }

}
