package io.fairyproject.tests.bukkit;

import be.seeseemelk.mockbukkit.ServerMock;
import io.fairyproject.tests.TestingHandle;
import org.jetbrains.annotations.NotNull;

/**
 *
 * This is the bukkit implementation for testing handle
 * You can use this to create custom server mock.
 *
 */
public interface BukkitTestingHandle extends TestingHandle {

    @NotNull
    ServerMock createServerMock();

}
