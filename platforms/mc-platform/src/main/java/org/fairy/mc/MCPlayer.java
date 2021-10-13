package org.fairy.mc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.fairy.mc.message.MCMessage;
import org.fairy.mc.protocol.MCPacket;
import org.fairy.mc.protocol.MCProtocol;
import org.fairy.mc.protocol.netty.FriendlyByteBuf;
import org.fairy.mc.title.MCTitle;
import org.fairy.metadata.CommonMetadataRegistries;
import org.fairy.metadata.MetadataKey;

import java.util.UUID;
import java.util.function.Function;

/**
 * A proxy player class for cross-platform purposes
 */
public interface MCPlayer {

    MetadataKey<MCPlayer> METADATA = MetadataKey.create("proxy:player", MCPlayer.class);

    static <T> MCPlayer from(T originalPlayer) {
        return CommonMetadataRegistries
                .provide(Companion.PLAYER_TO_UUID.apply(originalPlayer))
                .getOrThrow(METADATA);
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
     * send message to the player
     *
     * @param message the message
     */
    void sendMessage(String message);

    /**
     * send message to the player
     *
     * @param messages the messages
     */
    void sendMessage(String... messages);

    /**
     * send message to the player
     *
     * @param messages the messages
     */
    void sendMessage(Iterable<String> messages);

    /**
     * send message to the player
     *
     * @param message the message
     */
    default void sendMessage(MCMessage message) {
        this.sendMessage(message.get(this));
    }

    /**
     * send title to the player
     *
     * @param title the title
     */
    void sendTitle(MCTitle title);

    /**
     * get netty channel
     *
     * @return netty channel
     */
    Channel getChannel();

    /**
     * send packet to the player
     *
     * @param packet the packet
     */
    default void sendPacket(MCPacket packet) {
        final int id = MCProtocol.INSTANCE.getProtocolMapping().fromPacketClass(packet.getClass());

        final ByteBuf buffer = this.getChannel().alloc().buffer();
        final FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(buffer);

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

        public static Function<Object, UUID> PLAYER_TO_UUID = null;

    }

}
