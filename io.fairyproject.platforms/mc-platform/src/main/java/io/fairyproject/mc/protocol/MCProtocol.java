package io.fairyproject.mc.protocol;

import com.github.retrooper.packetevents.PacketEvents;
import io.fairyproject.mc.MCAdventure;
import lombok.Getter;
import io.fairyproject.mc.protocol.netty.NettyInjector;

@Getter
public class MCProtocol {

    public static MCProtocol INSTANCE;
    public static void initialize(NettyInjector injector) {
        new MCProtocol(injector);

        MCAdventure.initialize();
    }

    private final NettyInjector injector;

    private MCProtocol(NettyInjector injector) {
        INSTANCE = this;

        this.injector = injector;
    }

    public MCVersion version() {
        return MCVersion.getVersionFromRaw(
                PacketEvents.getAPI().getServerManager().getVersion().getProtocolVersion()
        );
    }

}
