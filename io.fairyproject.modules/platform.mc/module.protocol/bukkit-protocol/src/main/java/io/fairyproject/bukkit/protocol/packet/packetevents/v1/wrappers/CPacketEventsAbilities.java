package io.fairyproject.bukkit.protocol.packet.packetevents.v1.wrappers;

import io.fairyproject.bukkit.protocol.packet.packetevents.v1.PacketEventWrapper;
import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketAbilities;
import io.github.retrooper.packetevents.packetwrappers.play.in.abilities.WrappedPacketInAbilities;

import java.util.Optional;

public class CPacketEventsAbilities extends PacketEventWrapper<WrappedPacketInAbilities> implements CPacketAbilities {
    public CPacketEventsAbilities(WrappedPacketInAbilities wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public boolean isFlying() {
        return wrapper.isFlying();
    }

    @Override
    public Optional<Boolean> isVulnerable() {
        return wrapper.isVulnerable();
    }

    @Override
    public Optional<Boolean> isAllowFlight() {
        return wrapper.isFlightAllowed();
    }

    @Override
    public Optional<Boolean> isCreative() {
        return wrapper.canInstantlyBuild();
    }

    @Override
    public Optional<Float> getFlySpeed() {
        return wrapper.getFlySpeed();
    }

    @Override
    public Optional<Float> getWalkSpeed() {
        return wrapper.getWalkSpeed();
    }
}
