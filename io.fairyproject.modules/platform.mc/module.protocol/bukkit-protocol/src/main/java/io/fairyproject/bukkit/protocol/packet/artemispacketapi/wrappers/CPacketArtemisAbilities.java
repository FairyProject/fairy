package io.fairyproject.bukkit.protocol.packet.artemispacketapi.wrappers;

import io.fairyproject.bukkit.protocol.packet.artemispacketapi.ArtemisPacketWrapper;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.packet.client.CPacketAbilities;
import cc.ghast.packet.wrapper.packet.play.client.GPacketPlayClientAbilities;

import java.util.Optional;

public class CPacketArtemisAbilities extends ArtemisPacketWrapper<GPacketPlayClientAbilities> implements CPacketAbilities {
    public CPacketArtemisAbilities(GPacketPlayClientAbilities wrapper, MCPlayer channel) {
        super(wrapper, channel);
    }

    @Override
    public boolean isFlying() {
        return wrapper.isFlying();
    }

    @Override
    public Optional<Boolean> isVulnerable() {
        return wrapper.getInvulnerable();
    }

    @Override
    public Optional<Boolean> isAllowFlight() {
        return wrapper.getAllowedFlight();
    }

    @Override
    public Optional<Boolean> isCreative() {
        return wrapper.getCreativeMode();
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
