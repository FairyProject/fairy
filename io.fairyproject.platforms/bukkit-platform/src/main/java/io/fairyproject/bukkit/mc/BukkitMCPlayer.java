package io.fairyproject.bukkit.mc;

import io.fairyproject.bukkit.FairyBukkitPlatform;
import io.fairyproject.bukkit.reflection.MinecraftReflection;
import io.fairyproject.bukkit.reflection.resolver.FieldResolver;
import io.fairyproject.bukkit.reflection.resolver.minecraft.NMSClassResolver;
import io.fairyproject.bukkit.reflection.wrapper.FieldWrapper;
import io.fairyproject.bukkit.util.PlayerLocaleUtil;
import io.fairyproject.mc.GameMode;
import io.fairyproject.mc.MCAdventure;
import io.fairyproject.mc.MCGameProfile;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.MCVersion;
import io.fairyproject.mc.util.AudienceProxy;
import io.fairyproject.util.trial.TrialBiConsumers;
import io.fairyproject.util.trial.TrialFunctions;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class BukkitMCPlayer extends AudienceProxy implements MCPlayer {

    private static final AttributeKey<Object> ATTRIBUTE_KEY = AttributeKey.valueOf("protocol");
    private static final Function<Player, Integer> PING;
    private static final Function<Player, Component> GET_DISPLAY_NAME;
    private static final Function<Player, MCGameProfile> GAME_PROFILE;
    private static final BiConsumer<MCPlayer, Component> SET_DISPLAY_NAME;
    private static FieldWrapper<Integer> PROTOCOL_ID_FIELD;

    static {
        try {
            PING = TrialFunctions.<Player, Integer>create()
                    .trial(() -> Player.class.getMethod("getPing"), NoSuchMethodException.class, player -> player.getPing())
                    .find(MinecraftReflection::getPing)
                    .unchecked();

            GAME_PROFILE = TrialFunctions.<Player, MCGameProfile>create()
                    .trial(() -> Player.class.getMethod("getPlayerProfile"), NoSuchMethodException.class, player -> new io.fairyproject.bukkit.mc.PaperMCGameProfile(player.getPlayerProfile()))
                    .find(player -> MCGameProfile.from(MinecraftReflection.getGameProfile(player)))
                    .unchecked();

            GET_DISPLAY_NAME = TrialFunctions.<Player, Component>create()
                    .trial(() -> Player.class.getMethod("displayName"), NoSuchMethodException.class, player -> player.displayName())
                    .find(player -> MCAdventure.LEGACY.deserialize(player.getDisplayName()))
                    .unchecked();

            SET_DISPLAY_NAME = TrialBiConsumers.<MCPlayer, Component>create()
                    .trial(() -> Player.class.getMethod("displayName", Component.class), NoSuchMethodException.class, (player, component) -> player.as(Player.class).displayName(component))
                    .find((player, component) -> player.as(Player.class).setDisplayName(MCAdventure.asLegacyString(component, player.getLocale())))
                    .unchecked();
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

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
                    final NMSClassResolver classResolver = new NMSClassResolver();
                    PROTOCOL_ID_FIELD = new FieldResolver(classResolver.resolve("network.EnumProtocol","EnumProtocol")).resolveByFirstTypeDynamic(int.class);
                    if (!PROTOCOL_ID_FIELD.exists()) {
                        throw new IllegalStateException("Field does not exists!");
                    }
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Failed to retrieve EnumProtocol ID field! report it to fairy", e);
                }
            }

            this.protocolId = PROTOCOL_ID_FIELD.get(nmsProtocol);
        }
        return this.protocolId;
    }

    @Override
    public MCVersion getVersion() {
        return MinecraftReflection.getProtocol(this.player);
    }

    @Override
    public int ping() {
        return PING.apply(this.player);
    }

    @Override
    public GameMode gameMode() {
        return GameMode.valueOf(this.player.getGameMode().name());
    }

    @Override
    public MCGameProfile gameProfile() {
        return GAME_PROFILE.apply(this.player);
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
    public Component getDisplayName() {
        return GET_DISPLAY_NAME.apply(this.player);
    }

    @Override
    public void setDisplayName(Component component) {
        SET_DISPLAY_NAME.accept(this, component);
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
