package io.fairyproject.mc;

import io.fairyproject.mc.protocol.MCPacket;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.protocol.netty.FriendlyByteBuf;
import io.fairyproject.metadata.CommonMetadataRegistries;
import io.fairyproject.metadata.MetadataKey;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Locale;
import java.util.UUID;

/**
 * A proxy player class for cross-platform purposes
 */
public interface MCPlayer extends Audience {

    MetadataKey<MCPlayer> METADATA = MetadataKey.create("proxy:player", MCPlayer.class);

    static <T> MCPlayer from(T originalPlayer) {
        return CommonMetadataRegistries
                .provide(Companion.BRIDGE.from(originalPlayer))
                .getOrPut(METADATA, () -> Companion.BRIDGE.create(originalPlayer));
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
     * get player's locale
     *
     * @return locale
     */
    String getLocale();

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
    default void sendPacket(MCPacket packet) {
        final int id = MCProtocol.INSTANCE.getProtocolMapping()
                .getProtocol(getProtocolId())
                .fromPacketClass(packet.getClass());

        final ByteBuf buffer = this.getChannel().alloc().buffer();
        final FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(buffer);
        friendlyByteBuf.setLocale(Locale.forLanguageTag(this.getLocale()));

        friendlyByteBuf.writeVarInt(id);
        packet.write(friendlyByteBuf);

        sendRawPacket(friendlyByteBuf);
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
            this.getChannel().pipeline().context(MCProtocol.INSTANCE.getInjector().getEncoderName()).writeAndFlush(packet);
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

    }

    interface Bridge {

        UUID from(Object obj);

        MCPlayer create(Object obj);

    }

}
