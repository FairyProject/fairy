package io.fairyproject.mc.protocol.event;

import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import io.fairyproject.event.Cancellable;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.event.trait.MCPlayerEvent;
import org.jetbrains.annotations.NotNull;

public class MCPlayerProtocolPacketEvent implements MCPlayerEvent, Cancellable {

    private final MCPlayer player;
    private final ProtocolPacketEvent event;

    public MCPlayerProtocolPacketEvent(MCPlayer player, ProtocolPacketEvent event) {
        this.player = player;
        this.event = event;
    }

    @Override
    public @NotNull MCPlayer getPlayer() {
        return this.player;
    }

    public ConnectionState connectionState() {
        return this.event.getConnectionState();
    }

    public ServerVersion serverVersion() {
        return this.event.getServerVersion();
    }

    public PacketTypeCommon packetType() {
        return this.event.getPacketType();
    }

    public ProtocolPacketEvent getEvent() {
        return event;
    }

    @Override
    public boolean isCancelled() {
        return this.event.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.event.setCancelled(cancelled);
    }
}
