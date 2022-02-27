package io.fairyproject.mc.protocol;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import io.fairyproject.mc.MCAdventure;
import io.fairyproject.plugin.Plugin;
import lombok.Getter;
import io.fairyproject.mc.protocol.mapping.MCProtocolMapping;
import io.fairyproject.mc.protocol.netty.NettyInjector;

@Getter
public class MCProtocol extends PacketEventsAPI<Plugin> {

    public static MCProtocol INSTANCE;
    public static void initialize(NettyInjector injector, MCProtocolMapping protocolMapping) {
        new MCProtocol(injector, protocolMapping);

        MCAdventure.initialize();
    }

    private final NettyInjector injector;
    private final MCProtocolMapping protocolMapping;

    private MCProtocol(NettyInjector injector, MCProtocolMapping protocolMapping) {
        INSTANCE = this;

        this.injector = injector;
        this.protocolMapping = protocolMapping;
    }

    public MCVersion version() {
        return this.protocolMapping.getVersion();
    }

}
