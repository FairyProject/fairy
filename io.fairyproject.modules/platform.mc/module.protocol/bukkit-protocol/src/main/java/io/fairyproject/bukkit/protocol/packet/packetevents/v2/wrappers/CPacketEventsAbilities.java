package io.fairyproject.bukkit.protocol.packet.packetevents.v2.wrappers;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerAbilities;
import io.fairyproject.bukkit.protocol.packet.packetevents.v2.PacketEventWrapper;
import io.fairyproject.mc.protocol.netty.Channel;
import io.fairyproject.mc.protocol.packet.client.CPacketAbilities;

import java.util.Optional;

public class CPacketEventsAbilities extends PacketEventWrapper<WrapperPlayClientPlayerAbilities> implements CPacketAbilities {
    public CPacketEventsAbilities(WrapperPlayClientPlayerAbilities wrapper, Channel channel) {
        super(wrapper, channel);
    }

    @Override
    public boolean isFlying() {
        return wrapper.isFlying();
    }

    @Override
    public Optional<Boolean> isVulnerable() {
        return wrapper.isInGodMode();
    }

    @Override
    public Optional<Boolean> isAllowFlight() {
        return wrapper.isFlightAllowed();
    }

    @Override
    public Optional<Boolean> isCreative() {
        return wrapper.isInCreativeMode();
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
