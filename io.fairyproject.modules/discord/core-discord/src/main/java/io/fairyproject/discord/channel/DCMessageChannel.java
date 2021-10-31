package io.fairyproject.discord.channel;

import io.fairyproject.discord.DCBot;
import io.fairyproject.discord.proxies.ProxyMessageChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class DCMessageChannel implements ProxyMessageChannel {

    private final MessageChannel original;
    private final DCBot bot;

    public DCMessageChannel(MessageChannel original, DCBot bot) {
        this.original = original;
        this.bot = bot;
    }

    @Override
    public MessageChannel original() {
        return this.original;
    }

    public CompletableFuture<Message> readNextMessage(@NotNull User targetUser) {
        return this.bot.getNextMessageReader().read(this, targetUser);
    }

    public CompletableFuture<Message> readNextMessage(long targetUserIdLong) {
        return this.bot.getNextMessageReader().read(this, targetUserIdLong);
    }

    public CompletableFuture<Message> readNextMessage() {
        return this.bot.getNextMessageReader().read(this, null);
    }

    public CompletableFuture<Message> readNextMessage(@NotNull User targetUser, long expiration, TimeUnit timeUnit) {
        return this.bot.getNextMessageReader().read(this, targetUser, expiration, timeUnit);
    }

    public CompletableFuture<Message> readNextMessage(long targetUserIdLong, long expiration, TimeUnit timeUnit) {
        return this.bot.getNextMessageReader().read(this, targetUserIdLong, expiration, timeUnit);
    }

    public CompletableFuture<Message> readNextMessage(long expiration, TimeUnit timeUnit) {
        return this.bot.getNextMessageReader().read(this, null, expiration, timeUnit);
    }

}
