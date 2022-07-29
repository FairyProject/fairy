package io.fairyproject.mc.protocol.impl.mock;

import com.github.retrooper.packetevents.netty.NettyManager;
import com.github.retrooper.packetevents.netty.buffer.ByteBufAllocationOperator;
import com.github.retrooper.packetevents.netty.buffer.ByteBufOperator;
import com.github.retrooper.packetevents.netty.channel.ChannelOperator;
import io.github.retrooper.packetevents.netty.buffer.ByteBufAllocationOperatorModernImpl;
import io.github.retrooper.packetevents.netty.buffer.ByteBufOperatorModernImpl;
import io.github.retrooper.packetevents.netty.channel.ChannelOperatorModernImpl;

public class MockNettyManager implements NettyManager {
    @Override
    public ChannelOperator getChannelOperator() {
        return new ChannelOperatorModernImpl();
    }

    @Override
    public ByteBufOperator getByteBufOperator() {
        return new ByteBufOperatorModernImpl();
    }

    @Override
    public ByteBufAllocationOperator getByteBufAllocationOperator() {
        return new ByteBufAllocationOperatorModernImpl();
    }
}
