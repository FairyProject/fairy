package io.fairyproject.mc;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.fairyproject.mc.protocol.MCPacket;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.protocol.MCVersion;
import io.fairyproject.mc.protocol.item.PlayerInfoData;
import io.fairyproject.mc.protocol.netty.FriendlyByteBuf;
import io.fairyproject.metadata.CommonMetadataRegistries;
import io.fairyproject.metadata.MetadataKey;
import io.fairyproject.metadata.MetadataMap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.translation.Translator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;

/**
 * A proxy player class for cross-platform purposes
 */
public interface MCPlayer extends Audience {

    MetadataKey<MCPlayer> METADATA = MetadataKey.create("proxy:player", MCPlayer.class);

    static Collection<MCPlayer> all() {
        return Companion.BRIDGE.all();
    }

    @NotNull
    static <T> MCPlayer from(T originalPlayer) {
        return CommonMetadataRegistries
                .provide(Companion.BRIDGE.from(originalPlayer))
                .getOrPut(METADATA, () -> Companion.BRIDGE.create(originalPlayer));
    }

    @Nullable
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
    int ping();

    /**
     * get player's game mode
     *
     * @return game mode
     */
    GameMode gameMode();

    /**
     * get player's game profile
     *
     * @return game profile;
     */
    MCGameProfile gameProfile();

    default PlayerInfoData asInfoData() {
        return new PlayerInfoData(this.ping(), this.gameProfile(), this.gameMode(), this.getDisplayName());
    }

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

    /**
     * get player's current protocol id
     *
     * @return protocol id
     */
    int getProtocolId();

    /**
     * send packet to the player
     *
     * @param packet the packet
     */
    default void sendPacket(PacketWrapper<?> packet) {
        PacketEvents.getAPI().getProtocolManager().sendPacket(this.getChannel(), packet);
    }

    /**
     * send raw bytebuf packet to the player
     *
     * @param packet the packet
     */
    default void sendRawPacket(ByteBuf packet) {
        this.sendRawPacket(packet, false);
    }

    /**
     * send raw bytebuf packet to the player
     *
     * @param packet the packet
     * @param currentThread should write in current thread
     */
    default void sendRawPacket(ByteBuf packet, boolean currentThread) {
        Runnable runnable = () -> {
            PacketEvents.getAPI().getProtocolManager().sendPacket(this.getChannel(), packet);
        };

        if (currentThread) {
            runnable.run();
        } else {
            try {
                this.getChannel().eventLoop().submit(runnable);
            } catch (Throwable throwable) {
                packet.release();
                throwable.printStackTrace();
            }
        }
    }

    /**
     * cast the proxy player to platform specific player instance
     *
     * @param playerClass the platform specific Player class
     * @param <T> the type of the platform specific Player
     * @return the instance
     * @throws ClassCastException if class is incorrect, could be wrong type or not the right platform
     */
    <T> T as(Class<T> playerClass);

    class Companion {

        public static Bridge BRIDGE = null;
        public static Function<MCPlayer, Locale> GET_LOCALE = mcPlayer -> Translator.parseLocale(mcPlayer.getGameLocale());

    }

    interface Bridge {

        UUID from(Object obj);

        MCPlayer find(UUID uuid);

        MCPlayer create(Object obj);

        Collection<MCPlayer> all();

    }

}
