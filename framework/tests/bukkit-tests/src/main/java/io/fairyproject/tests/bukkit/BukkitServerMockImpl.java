package io.fairyproject.tests.bukkit;

import be.seeseemelk.mockbukkit.ServerMock;
import org.bukkit.Warning;
import org.jetbrains.annotations.NotNull;

public class BukkitServerMockImpl extends ServerMock {

    @Override
    public Warning.@NotNull WarningState getWarningState() {
        return Warning.WarningState.DEFAULT;
    }
}
