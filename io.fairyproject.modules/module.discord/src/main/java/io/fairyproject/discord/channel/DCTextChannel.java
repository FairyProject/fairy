package io.fairyproject.discord.channel;

import io.fairyproject.discord.DCBot;
import io.fairyproject.discord.proxies.ProxyTextChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class DCTextChannel implements ProxyTextChannel {

    private final TextChannel textChannel;
    private final DCBot bot;

    public DCTextChannel(TextChannel original, DCBot bot) {
        this.textChannel = original;
        this.bot = bot;
    }

    @Override
    public TextChannel original() {
        return this.textChannel;
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
