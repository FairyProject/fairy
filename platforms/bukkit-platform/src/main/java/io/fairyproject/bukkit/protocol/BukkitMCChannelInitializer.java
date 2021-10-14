package io.fairyproject.bukkit.protocol;

import io.fairyproject.bukkit.protocol.viaversion.ViaVersionBukkitDecoder;
import io.fairyproject.bukkit.protocol.viaversion.ViaVersionBukkitEncoder;
import io.fairyproject.bukkit.reflection.resolver.FieldResolver;
import io.fairyproject.bukkit.reflection.wrapper.FieldWrapper;
import io.fairyproject.bukkit.reflection.wrapper.MethodWrapper;
import io.fairyproject.util.AccessUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import org.bukkit.Bukkit;

import java.lang.reflect.Method;

public class BukkitMCChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final ChannelInitializer<SocketChannel> original;
    private MethodWrapper<?> method;

    public BukkitMCChannelInitializer(ChannelInitializer<SocketChannel> oldInit) {
        this.original = oldInit;
        try {
            final Method initChannel = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
            AccessUtil.setAccessible(initChannel);
            this.method = new MethodWrapper<>(initChannel);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public ChannelInitializer<SocketChannel> getOriginal() {
        return original;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        this.method.invoke(this.original, socketChannel);

        MessageToByteEncoder oldEncoder = (MessageToByteEncoder) socketChannel.pipeline().get("encoder");
        ByteToMessageDecoder oldDecoder = (ByteToMessageDecoder) socketChannel.pipeline().get("decoder");

        // Via version compatibility
        MessageToByteEncoder viaEncoder = null;
        ByteToMessageDecoder viaDecoder = null;
        if (Bukkit.getPluginManager().isPluginEnabled("ViaVersion")) {
            initViaVersionCompatibility();
            if (VIA_ENCODER_CLASS.isInstance(oldEncoder)) {
                viaEncoder = oldEncoder;
                oldEncoder = (MessageToByteEncoder) VIA_ENCODER_ORIGINAL_FIELD.get(viaEncoder);
            }
            if (VIA_DECODER_CLASS.isInstance(oldEncoder)) {
                viaDecoder = oldDecoder;
                oldDecoder = (ByteToMessageDecoder) VIA_DECODER_ORIGINAL_FIELD.get(viaDecoder);
            }
        }

        MessageToByteEncoder newEncoder = new BukkitMCEncoder(oldEncoder);
        ByteToMessageDecoder newDecoder = new BukkitMCDecoder(oldDecoder);

        // Via version compatibility
        if (viaEncoder != null) {
            newEncoder = new ViaVersionBukkitEncoder(VIA_ENCODER_INFO_FIELD.get(viaEncoder), (BukkitMCEncoder) newEncoder);
        }
        if (viaDecoder != null) {
            newDecoder = new ViaVersionBukkitDecoder(VIA_DECODER_INFO_FIELD.get(viaDecoder), (BukkitMCDecoder) newDecoder);
        }

        socketChannel.pipeline().replace("encoder", "encoder", newEncoder);
        socketChannel.pipeline().replace("decoder", "decoder", newDecoder);
    }

    // Via version compatibility
    private static Class<? extends MessageToByteEncoder> VIA_ENCODER_CLASS;
    private static Class<? extends ByteToMessageDecoder> VIA_DECODER_CLASS;

    private static FieldWrapper<?> VIA_ENCODER_ORIGINAL_FIELD, VIA_ENCODER_INFO_FIELD;
    private static FieldWrapper<?> VIA_DECODER_ORIGINAL_FIELD, VIA_DECODER_INFO_FIELD;
    public static void initViaVersionCompatibility() {
        if (VIA_ENCODER_CLASS != null) {
            return;
        }
        try {
            VIA_ENCODER_CLASS = (Class<? extends MessageToByteEncoder>) Class.forName("com.viaversion.viaversion.bukkit.handlers.BukkitEncodeHandler");
            VIA_DECODER_CLASS = (Class<? extends ByteToMessageDecoder>) Class.forName("com.viaversion.viaversion.bukkit.handlers.BukkitDecodeHandler");

            FieldResolver fieldResolver = new FieldResolver(VIA_ENCODER_CLASS);
            VIA_ENCODER_ORIGINAL_FIELD = fieldResolver.resolveByFirstTypeDynamic(MessageToByteEncoder.class);
            VIA_ENCODER_INFO_FIELD = fieldResolver.resolveWrapper("info");

            fieldResolver = new FieldResolver(VIA_DECODER_CLASS);
            VIA_DECODER_ORIGINAL_FIELD = fieldResolver.resolveByFirstTypeDynamic(ByteToMessageDecoder.class);
            VIA_DECODER_INFO_FIELD = fieldResolver.resolveWrapper("info");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
