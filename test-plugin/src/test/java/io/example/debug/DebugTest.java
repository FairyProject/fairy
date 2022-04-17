package io.example.debug;

import io.fairyproject.tests.RuntimeMode;
import io.fairyproject.tests.bukkit.base.BukkitJUnitJupiterBase;
import org.junit.jupiter.api.Test;

public class DebugTest extends BukkitJUnitJupiterBase {

    @Test
    public void a() {
    }

    @Test
    public void b() {
    }

    @Override
    public RuntimeMode runtimeMode() {
        return RuntimeMode.BEFORE_EACH;
    }
}
