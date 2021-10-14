package io.fairyproject.mc.protocol;

import lombok.Getter;
import io.fairyproject.mc.protocol.mapping.MCProtocolMapping;
import io.fairyproject.mc.protocol.netty.NettyInjector;

@Getter
public class MCProtocol {

    public static MCProtocol INSTANCE;
    public static void initialize(NettyInjector injector, MCProtocolMapping protocolMapping) {
        new MCProtocol(injector, protocolMapping);
    }

    private final NettyInjector injector;
    private final MCProtocolMapping protocolMapping;

    private MCProtocol(NettyInjector injector, MCProtocolMapping protocolMapping) {
        INSTANCE = this;

        this.injector = injector;
        this.protocolMapping = protocolMapping;
    }

}
