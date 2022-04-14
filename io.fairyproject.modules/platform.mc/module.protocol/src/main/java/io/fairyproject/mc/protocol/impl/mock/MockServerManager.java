package io.fairyproject.mc.protocol.impl.mock;

import com.github.retrooper.packetevents.manager.server.ServerManager;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import io.fairyproject.mc.protocol.MCProtocol;

public class MockServerManager implements ServerManager {
    @Override
    public ServerVersion getVersion() {
        return ServerVersion.getById(MCProtocol.OVERWRITTEN_VERSION.getRawVersion()[0]);
    }
}
