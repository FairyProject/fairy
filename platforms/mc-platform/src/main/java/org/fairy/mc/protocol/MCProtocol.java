package org.fairy.mc.protocol;

import lombok.Getter;
import org.fairy.mc.protocol.mapping.MCProtocolMapping;
import org.fairy.mc.protocol.netty.NettyInjector;

@Getter
public class MCProtocol {

    public static MCProtocol INSTANCE;

    private final NettyInjector injector;
    private final MCProtocolMapping protocolMapping;

    public MCProtocol(NettyInjector injector, MCProtocolMapping protocolMapping) {
        INSTANCE = this;

        this.injector = injector;
        this.protocolMapping = protocolMapping;
    }

}
