package io.fairyproject.bukkit.player;

import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.bukkit.reflection.MinecraftReflection;
import io.fairyproject.bukkit.reflection.resolver.FieldResolver;
import io.fairyproject.bukkit.reflection.wrapper.FieldWrapper;
import io.fairyproject.bukkit.util.PlayerLocaleUtil;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.util.AudienceProxy;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BukkitMCPlayer extends AudienceProxy implements MCPlayer {

    private static final AttributeKey<Object> ATTRIBUTE_KEY = AttributeKey.valueOf("protocol");
    private static FieldWrapper<?> PROTOCOL_ID_FIELD;

    private final Player player;
    private final Channel channel;
    private final Audience audience;

    private Object nmsProtocol;
    private int protocolId;

    public BukkitMCPlayer(Player player) {
        this.player = player;
        this.audience = FairyBukkitPlatform.AUDIENCES.player(player);
        this.channel = MinecraftReflection.getChannel(player);
    }

    public int getProtocolId() {
        Object nmsProtocol = this.channel.attr(ATTRIBUTE_KEY).get();
        // Only attempt to retrieve EnumProtocol ID whenever the protocol instance changed, reduce reflection usages
        if (this.nmsProtocol != nmsProtocol) {
            this.nmsProtocol = nmsProtocol;

            if (PROTOCOL_ID_FIELD == null) {
                try {
                    PROTOCOL_ID_FIELD = new FieldResolver(nmsProtocol.getClass()).resolveByFirstTypeDynamic(int.class);
                    if (!PROTOCOL_ID_FIELD.exists()) {
                        throw new IllegalStateException("Field does not exists!");
                    }
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Failed to retrieve EnumProtocol ID field! report it to fairy", e);
                }
            }

            this.protocolId = (int) PROTOCOL_ID_FIELD.get(nmsProtocol);
        }
        return this.protocolId;
    }

    @Override
    public UUID getUUID() {
        return this.player.getUniqueId();
    }

    @Override
    public String getName() {
        return this.player.getName();
    }

    @Override
    public boolean isOnline() {
        return this.player.isOnline();
    }

    @Override
    public String getGameLocale() {
        return PlayerLocaleUtil.getLocale(this.player);
    }

    @Override
    public Channel getChannel() {
        return this.channel;
    }

    @Override
    public <T> T as(Class<T> playerClass) {
        if (!playerClass.isInstance(this.player)) {
            throw new ClassCastException();
        }
        return playerClass.cast(this.player);
    }


    @Override
    public Audience audience() {
        return this.audience;
    }
}
