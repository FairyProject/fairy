package io.fairyproject.bukkit.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BukkitMCEncoder extends MessageToByteEncoder implements BukkitByteBufProcessor {

    private static final Method ENCODE_METHOD;
    static {
        try {
            ENCODE_METHOD = MessageToByteEncoder.class.getDeclaredMethod("encode", ChannelHandlerContext.class, Object.class, ByteBuf.class);
            ENCODE_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private final MessageToByteEncoder minecraftEncoder;

    public BukkitMCEncoder(MessageToByteEncoder minecraftEncoder) {
        this.minecraftEncoder = minecraftEncoder;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if (o instanceof ByteBuf) {
            byteBuf.writeBytes((ByteBuf) o);
        } else {
            try {
                ENCODE_METHOD.invoke(this.minecraftEncoder, new ChannelHandlerContextWrapper(channelHandlerContext, this), o, byteBuf);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof Exception) {
                    throw (Exception) e.getCause();
                } else if (e.getCause() instanceof Error) {
                    throw (Error) e.getCause();
                }
            }
        }

        this.process(byteBuf);
    }
    public void encodePublic(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        this.encode(channelHandlerContext, o, byteBuf);
    }

    @Override
    public void process(ByteBuf byteBuf) {
        // TODO
    }
}
