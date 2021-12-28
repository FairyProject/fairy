package io.fairyproject.tests.bukkit;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import be.seeseemelk.mockbukkit.ServerMock;
import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.tests.TestingBase;
import org.junit.BeforeClass;

import java.lang.reflect.InvocationTargetException;

public class BukkitTestingBase {

    protected static ServerMock SERVER;
    protected static MockPlugin PLUGIN;

    @BeforeClass
    public static void setup() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        SERVER = MockBukkit.getOrCreateMock();
        PLUGIN = MockBukkit.createMockPlugin();

        FairyBukkitPlatform.PLUGIN = PLUGIN;
        TestingBase.setup();
    }

}
