package io.fairyproject.discord.proxies;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.jetbrains.annotations.NotNull;

public interface ProxyMessageChannel extends MessageChannel {

    MessageChannel original();

    @Override
    default long getLatestMessageIdLong() {
        return original().getLatestMessageIdLong();
    }

    @Override
    default boolean hasLatestMessage() {
        return original().hasLatestMessage();
    }

    @NotNull
    @Override
    default String getName() {
        return original().getName();
    }

    @NotNull
    @Override
    default ChannelType getType() {
        return original().getType();
    }

    @NotNull
    @Override
    default JDA getJDA() {
        return original().getJDA();
    }

    @Override
    default long getIdLong() {
        return original().getIdLong();
    }
}
