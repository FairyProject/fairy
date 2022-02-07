package io.fairyproject.tests.bukkit;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import be.seeseemelk.mockbukkit.ServerMock;
import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.bukkit.util.JavaPluginUtil;
import io.fairyproject.tests.TestingBase;
import io.fairyproject.tests.TestingHandle;
import org.junit.jupiter.api.BeforeAll;

public abstract class BukkitTestingBase {

    protected static ServerMock SERVER;
    protected static MockPlugin PLUGIN;

    @BeforeAll
    public static void setup() {
        try {
            if (TestingBase.isInitialized()) {
                return;
            }

            ServerMock serverMock = null;
            TestingHandle testingHandle = TestingBase.findTestingHandle();
            if (testingHandle instanceof BukkitTestingHandle) {
                serverMock = ((BukkitTestingHandle) testingHandle).createServerMock();
            }

            if (!MockBukkit.isMocked()) {
                SERVER = MockBukkit.mock(serverMock == null ? new BukkitServerMockImpl() : serverMock);
            }
            PLUGIN = MockBukkit.createMockPlugin();

            FairyBukkitPlatform.PLUGIN = PLUGIN;
            JavaPluginUtil.setCurrentPlugin(PLUGIN);
            FairyBukkitTestingPlatform.patchBukkitPlugin(PLUGIN);

            TestingBase.setup(testingHandle);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

}
