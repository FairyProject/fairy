package io.fairytest;

import io.fairyproject.bukkit.reflection.minecraft.OBCVersion;
import io.fairyproject.mc.protocol.MCVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OBCVersionTest {

    @Test
    public void toMCVersion() {
        Assertions.assertEquals(MCVersion.V1_16, OBCVersion.v1_16_R3.toMCVersion());
        Assertions.assertEquals(MCVersion.V1_8, OBCVersion.v1_8_R3.toMCVersion());
    }

    @Test
    public void aboveAndBelow() {
        Assertions.assertTrue(OBCVersion.v1_14_R1.above(OBCVersion.v1_13_R2));
        Assertions.assertTrue(OBCVersion.v1_13_R2.below(OBCVersion.v1_14_R1));
        Assertions.assertTrue(OBCVersion.v1_13_R2.above(OBCVersion.v1_8_R3));
        Assertions.assertTrue(OBCVersion.v1_8_R3.below(OBCVersion.v1_13_R2));
    }

    @Test
    public void sanityCheck() {
        // TODO: automatically fetch any new OBC version tag from somewhere??
    }

}
