package io.fairyproject.tests.bukkit;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import be.seeseemelk.mockbukkit.ServerMock;
import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.bukkit.util.JavaPluginUtil;
import io.fairyproject.tests.TestingContext;
import io.fairyproject.tests.TestingHandle;
import lombok.Getter;
import lombok.SneakyThrows;

@Getter
public class MockBukkitContext {

    private static MockBukkitContext INSTANCE;

    public static MockBukkitContext get() {
        if (INSTANCE == null) {
            INSTANCE = new MockBukkitContext();
        }
        return INSTANCE;
    }

    private ServerMock server;
    private MockPlugin plugin;

    @SneakyThrows
    public void initialize() {
        ServerMock serverMock = null;
        TestingHandle testingHandle = TestingContext.get().findTestingHandle();
        if (testingHandle instanceof BukkitTestingHandle) {
            serverMock = ((BukkitTestingHandle) testingHandle).createServerMock();
        }

        if (!MockBukkit.isMocked()) {
            this.server = MockBukkit.mock(serverMock == null ? new BukkitServerMockImpl() : serverMock);
        }
        this.plugin = MockBukkit.createMockPlugin();

        FairyBukkitPlatform.PLUGIN = plugin;
        JavaPluginUtil.setCurrentPlugin(plugin);
        FairyBukkitTestingPlatform.patchBukkitPlugin(plugin);
    }

}
