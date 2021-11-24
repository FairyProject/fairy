package io.fairytest.gradle;

import io.fairyproject.gradle.util.MavenUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class MavenUtilTest {

    @Test
    public void existingModuleCheck() throws IOException {
        Assert.assertTrue(MavenUtil.isExistingModule("bukkit-menu"));
        Assert.assertFalse(MavenUtil.isExistingModule("bukkit-discord"));
    }

}
