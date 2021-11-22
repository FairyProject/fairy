package io.fairytest.aspect;

import io.fairyproject.util.FairyVersion;
import org.junit.Assert;
import org.junit.Test;

import java.util.TreeSet;

public class FairyVersionTest {

    @Test
    public void checkMajor() {
        FairyVersion lowerVersion = FairyVersion.parse("5.0.0b1");
        FairyVersion newerVersion = FairyVersion.parse("6.0.0b1");

        Assert.assertTrue(newerVersion.isAbove(lowerVersion));
    }

    @Test
    public void checkMinor() {
        FairyVersion lowerVersion = FairyVersion.parse("5.0.0b1");
        FairyVersion newerVersion = FairyVersion.parse("5.1.0b1");

        Assert.assertTrue(newerVersion.isAbove(lowerVersion));
    }

    @Test
    public void checkRevision() {
        FairyVersion lowerVersion = FairyVersion.parse("5.0.0b1");
        FairyVersion newerVersion = FairyVersion.parse("5.0.1b1");

        Assert.assertTrue(newerVersion.isAbove(lowerVersion));
    }

    @Test
    public void checkBuild() {
        FairyVersion lowerVersion = FairyVersion.parse("5.0.0b1");
        FairyVersion newerVersion = FairyVersion.parse("5.0.0b2");

        Assert.assertTrue(newerVersion.isAbove(lowerVersion));
    }

    @Test
    public void checkToString() {
        FairyVersion version = FairyVersion.parse("5.0.0b1");

        Assert.assertEquals(version.toString(), "5.0.0b1");
    }

    @Test
    public void checkTreeMap() {
        FairyVersion lowerVersion = FairyVersion.parse("5.0.0b1");
        FairyVersion newerVersion = FairyVersion.parse("5.0.1b1");

        TreeSet<FairyVersion> treeSet = new TreeSet<>();
        treeSet.add(newerVersion);
        treeSet.add(lowerVersion);

        Assert.assertEquals(newerVersion, treeSet.last());
    }

}
