package io.fairyproject.tests.bukkit;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import be.seeseemelk.mockbukkit.ServerMock;
import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.tests.TestingBase;
import org.junit.jupiter.api.BeforeAll;

public abstract class BukkitTestingBase {

    protected static ServerMock SERVER;
    protected static MockPlugin PLUGIN;

    @BeforeAll
    public static void setup() {
        try {
            if (!MockBukkit.isMocked()) {
                SERVER = MockBukkit.mock(new BukkitServerMockImpl());
            }
            PLUGIN = MockBukkit.createMockPlugin();

            FairyBukkitPlatform.PLUGIN = PLUGIN;
            TestingBase.setup();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

}
