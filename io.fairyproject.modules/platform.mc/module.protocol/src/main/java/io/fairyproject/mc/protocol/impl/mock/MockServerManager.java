package io.fairyproject.mc.protocol.impl.mock;

import com.github.retrooper.packetevents.manager.server.ServerManager;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import io.fairyproject.mc.MCServer;
import io.fairyproject.mc.version.MCVersion;
import io.fairyproject.mc.version.MCVersionMapping;
import io.fairyproject.mc.version.MCVersionMappingRegistry;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MockServerManager implements ServerManager {

    private final MCServer server;
    private final MCVersionMappingRegistry mappingRegistry;

    @Override
    public ServerVersion getVersion() {
        MCVersion version = server.getVersion();
        MCVersionMapping mapping = mappingRegistry.findMapping(version);

        return ServerVersion.getById(mapping.getProtocolVersion());
    }
}
