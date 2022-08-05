package io.fairyproject.mc.protocol.impl.mock;

import com.github.retrooper.packetevents.manager.server.ServerManager;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import io.fairyproject.mc.MCServer;

public class MockServerManager implements ServerManager {
    @Override
    public ServerVersion getVersion() {
        return ServerVersion.getById(MCServer.current().getVersion().getRawVersion()[0]);
    }
}
