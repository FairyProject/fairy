package io.fairyproject.tests.bukkit.base;

import be.seeseemelk.mockbukkit.MockPlugin;
import be.seeseemelk.mockbukkit.ServerMock;
import io.fairyproject.tests.TestingHandle;
import io.fairyproject.tests.base.JUnitJupiterBase;
import io.fairyproject.tests.bukkit.MockBukkitContext;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BukkitJUnitJupiterBase extends JUnitJupiterBase {

    private final AtomicBoolean mockBukkitInitialized = new AtomicBoolean(false);
    protected ServerMock server;
    protected MockPlugin plugin;

    @BeforeEach
    public void setupMockBukkit() {
        if (!mockBukkitInitialized.compareAndSet(false, true)) {
            return;
        }
        MockBukkitContext.get().initialize();
        this.server = MockBukkitContext.get().getServer();
        this.plugin = MockBukkitContext.get().getPlugin();
    }

    @Override
    public void initRuntime(TestingHandle testingHandle) {
        MockBukkitContext.get().initialize();
        super.initRuntime(testingHandle);
    }

}
