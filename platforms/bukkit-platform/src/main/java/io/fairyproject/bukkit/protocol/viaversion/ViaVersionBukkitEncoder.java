package io.fairyproject.bukkit.protocol.viaversion;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.bukkit.handlers.BukkitEncodeHandler;
import com.viaversion.viaversion.bukkit.util.NMSUtil;
import com.viaversion.viaversion.handlers.ChannelHandlerContextWrapper;
import io.fairyproject.bukkit.protocol.BukkitMCEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.lang.reflect.Field;

public class ViaVersionBukkitEncoder extends BukkitEncodeHandler {
    private static Field versionField;

    static {
        try {
            // Attempt to get any version info from the handler
            versionField = NMSUtil.nms(
                    "PacketEncoder",
                    "net.minecraft.network.PacketEncoder"
            ).getDeclaredField("version");

            versionField.setAccessible(true);
        } catch (Exception e) {
            // Not compat version
        }
    }

    private final BukkitMCEncoder fairyEncoder;

    public ViaVersionBukkitEncoder(Object info, BukkitMCEncoder fairyEncoder) {
        super((UserConnection) info, fairyEncoder);
        this.fairyEncoder = fairyEncoder;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object o, ByteBuf bytebuf) throws Exception {
        if (versionField != null) {
            versionField.set(fairyEncoder, versionField.get(this));
        }
        // handle the packet type
        if (!(o instanceof ByteBuf)) {
            // call minecraft encoder
            fairyEncoder.encodePublic(new ChannelHandlerContextWrapper(ctx, this), o, bytebuf);
        } else {
            bytebuf.writeBytes((ByteBuf) o);
        }
        transform(bytebuf);
    }
}
