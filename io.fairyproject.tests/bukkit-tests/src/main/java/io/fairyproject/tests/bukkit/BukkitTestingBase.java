package io.fairyproject.tests.bukkit;

import be.seeseemelk.mockbukkit.MockPlugin;
import be.seeseemelk.mockbukkit.ServerMock;
import io.fairyproject.tests.TestingContext;
import org.junit.jupiter.api.BeforeAll;

/**
 * @deprecated see {@link io.fairyproject.tests.bukkit.base.BukkitJUnitBase} and {@link io.fairyproject.tests.bukkit.base.BukkitJUnitJupiterBase}
 */
@Deprecated
public abstract class BukkitTestingBase {

    protected static ServerMock SERVER;
    protected static MockPlugin PLUGIN;

    @BeforeAll
    public static void setup() {
        if (TestingContext.get().isInitialized()) {
            return;
        }

        MockBukkitContext.get().initialize();
        SERVER = MockBukkitContext.get().getServer();
        PLUGIN = MockBukkitContext.get().getPlugin();

        TestingContext.get().initialize();
    }

}
