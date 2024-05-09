package io.fairyproject.mc;

import io.fairyproject.container.Containers;
import io.fairyproject.mc.registry.player.MCPlayerRegistry;
import io.fairyproject.mc.version.MCVersion;
import io.fairyproject.metadata.CommonMetadataRegistries;
import io.fairyproject.metadata.MetadataMap;
import io.netty.channel.Channel;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.translation.Translator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;

/**
 * A proxy player class for cross-platform purposes
 */
public interface MCPlayer extends MCEntity, Audience {

    @Deprecated
    static Collection<MCPlayer> all() {
        return Companion.BRIDGE.all();
    }

    @NotNull
    @Deprecated
    static <T> MCPlayer from(@Nullable T originalPlayer) {
        return Containers.get(MCPlayerRegistry.class).findPlayerByPlatformPlayer(originalPlayer);
    }

    @Nullable
    @Deprecated
    static MCPlayer find(UUID uuid) {
        return Companion.BRIDGE.find(uuid);
    }

    /**
     * get player's UUID
     *
     * @return UUID
     */
    UUID getUUID();

    /**
     * get player's Name
     *
     * @return name
     */
    String getName();

    /**
     * get player's IP address
     *
     * @return IP address
     */
    InetAddress getAddress();

    /**
     * is player online
     *
     * @return is online
     */
    boolean isOnline();

    /**
     * get player's display name
     *
     * @return display name
     */
    Component getDisplayName();

    /**
     * set player's display name
     *
     * @param component display name
     */
    void setDisplayName(Component component);

    /**
     * get client's protocol version
     *
     * @return protocol version
     */
    MCVersion getVersion();

    /**
     * get player's ping
     *
     * @return ping
     */
    int getPing();

    /**
     * get player's game mode
     *
     * @return game mode
     */
    GameMode getGameMode();

    /**
     * get player's game profile
     *
     * @return game profile;
     */
    MCGameProfile getGameProfile();

    /**
     * get metadata map for the player
     *
     * @return metadata map
     */
    default MetadataMap metadata() {
        return CommonMetadataRegistries.provide(this.getUUID());
    }

    /**
     * get player's in-game locale
     *
     * @return locale
     */
    String getGameLocale();

    /**
     * get player's locale
     *
     * @return locale
     */
    default Locale getLocale() {
        return Companion.GET_LOCALE.apply(this);
    }

    /**
     * send message to the player with legacy color code
     *
     * @param message the message
     */
    default void sendMessage(String message) {
        this.sendMessage(LegacyComponentSerializer.legacy('&').deserialize(message));
    }

    /**
     * send message to the player with legacy color code
     *
     * @param message the message
     */
    default void sendMessage(char colorCode, String message) {
        this.sendMessage(LegacyComponentSerializer.legacy(colorCode).deserialize(message));
    }

    /**
     * send message to the player with legacy color code
     *
     * @param messages the messages
     */
    default void sendMessage(String... messages) {
        for (String message : messages) {
            this.sendMessage(LegacyComponentSerializer.legacy('&').deserialize(message));
        }
    }

    /**
     * send message to the player with legacy color code
     *
     * @param messages the messages
     */
    default void sendMessage(Iterable<String> messages) {
        for (String message : messages) {
            this.sendMessage(LegacyComponentSerializer.legacy('&').deserialize(message));
        }
    }

    /**
     * send message to the player with legacy color code
     *
     * @param messages the messages
     */
    default void sendMessage(char colorCode, String... messages) {
        for (String message : messages) {
            this.sendMessage(LegacyComponentSerializer.legacy(colorCode).deserialize(message));
        }
    }

    /**
     * send message to the player with legacy color code
     *
     * @param messages the messages
     */
    default void sendMessage(char colorCode, Iterable<String> messages) {
        for (String message : messages) {
            this.sendMessage(LegacyComponentSerializer.legacy(colorCode).deserialize(message));
        }
    }

    /**
     * get netty channel
     *
     * @return netty channel
     */
    Channel getChannel();

    @Deprecated
    class Companion {

        public static Bridge BRIDGE = null;
        public static Function<MCPlayer, Locale> GET_LOCALE = mcPlayer -> Translator.parseLocale(mcPlayer.getGameLocale());

    }

    @Deprecated
    interface Bridge {

        UUID from(@NotNull Object obj);

        MCPlayer find(UUID uuid);

        MCPlayer create(Object obj);

        Collection<MCPlayer> all();

    }

}
