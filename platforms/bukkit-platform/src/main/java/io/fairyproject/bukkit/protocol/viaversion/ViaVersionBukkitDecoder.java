package io.fairyproject.bukkit.protocol.viaversion;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.bukkit.handlers.BukkitDecodeHandler;
import com.viaversion.viaversion.exception.CancelDecoderException;
import io.fairyproject.bukkit.protocol.BukkitMCDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class ViaVersionBukkitDecoder extends BukkitDecodeHandler {
    private final UserConnection info;
    private final BukkitMCDecoder fairyDecoder;

    public ViaVersionBukkitDecoder(Object info, BukkitMCDecoder fairyDecoder) {
        super((UserConnection) info, fairyDecoder);
        this.info = (UserConnection) info;
        this.fairyDecoder = fairyDecoder;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf bytebuf, List<Object> list) throws Exception {
        if (!info.checkIncomingPacket()) {
            bytebuf.clear(); // Don't accumulate
            throw CancelDecoderException.generate(null);
        }

        ByteBuf transformedBuf = null;
        try {
            if (info.shouldTransformPacket()) {
                transformedBuf = ctx.alloc().buffer().writeBytes(bytebuf);
                info.transformIncoming(transformedBuf, CancelDecoderException::generate);
            }

            try {
                List<Object> packets = new ArrayList<>();
                this.fairyDecoder.decodePublic(ctx, transformedBuf == null ? bytebuf : transformedBuf, packets);
                list.addAll(packets);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof Exception) {
                    throw (Exception) e.getCause();
                } else if (e.getCause() instanceof Error) {
                    throw (Error) e.getCause();
                }
            }
        } finally {
            if (transformedBuf != null) {
                transformedBuf.release();
            }
        }
    }
}
