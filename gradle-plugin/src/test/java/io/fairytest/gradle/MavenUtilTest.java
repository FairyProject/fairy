package io.fairytest.gradle;

import io.fairyproject.gradle.util.MavenUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class MavenUtilTest {

    @Test
    public void existingModuleCheck() throws IOException {
        Assertions.assertTrue(MavenUtil.isExistingModule("bukkit-menu"));
        Assertions.assertFalse(MavenUtil.isExistingModule("bukkit-discord"));
    }

}
