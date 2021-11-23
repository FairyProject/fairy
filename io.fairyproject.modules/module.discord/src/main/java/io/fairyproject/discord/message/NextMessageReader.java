package io.fairyproject.discord.message;

import com.google.common.collect.HashMultimap;
import io.fairyproject.discord.DCBot;
import io.fairyproject.task.Task;
import io.fairyproject.util.terminable.Terminable;
import lombok.Data;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NextMessageReader {

    private static final Exception TIMEOUT = new TimeoutException();

    private final HashMultimap<KeyPair, FuturePair> pending = HashMultimap.create();
    private final DCBot bot;

    public NextMessageReader(DCBot bot) {
        this.bot = bot;
        Task.asyncRepeated(this::cleanExpiration, 1L);
    }

    private void cleanExpiration(Terminable t) {
        if (this.bot.getStatus() == JDA.Status.DISCONNECTED) {
            t.closeAndReportException();
            return;
        }

        synchronized (this.pending) {
            final Iterator<Map.Entry<KeyPair, FuturePair>> iterator = this.pending.entries().iterator();
            final long timeMillis = System.currentTimeMillis();
            while (iterator.hasNext()) {
                final Map.Entry<KeyPair, FuturePair> entry = iterator.next();
                final FuturePair futurePair = entry.getValue();

                if (futurePair.getExpiration() > 0 && timeMillis > futurePair.getStart() + futurePair.getExpiration()) {
                    iterator.remove();
                    futurePair.getFuture().completeExceptionally(TIMEOUT);
                }
            }
        }
    }

    public CompletableFuture<Message> read(@NotNull MessageChannel channel, @Nullable User user) {
        return this.read(channel, user, -1, null);
    }

    public CompletableFuture<Message> read(@NotNull MessageChannel channel, @Nullable User user, long expiration, TimeUnit timeUnit) {
        return this.read(channel, user != null ? user.getIdLong() : -1, expiration, timeUnit);
    }

    public CompletableFuture<Message> read(@NotNull MessageChannel channel, long userIdLong) {
        return this.read(channel, userIdLong, -1, null);
    }

    public CompletableFuture<Message> read(@NotNull MessageChannel channel, long userIdLong, long expiration, TimeUnit timeUnit) {
        long guildId = -1;
        if (channel instanceof TextChannel) {
            guildId = ((TextChannel) channel).getGuild().getIdLong();
        }

        KeyPair keyPair = new KeyPair(guildId, channel.getIdLong(), userIdLong);
        CompletableFuture<Message> completableFuture = new CompletableFuture<>();
        FuturePair futurePair = new FuturePair(completableFuture, System.currentTimeMillis(), timeUnit != null ? timeUnit.toMillis(expiration) : expiration);

        synchronized (this.pending) {
            this.pending.put(keyPair, futurePair);
        }

        return completableFuture;
    }

    public boolean handleMessage(MessageReceivedEvent event) {
        User user = event.getAuthor();

        if (user.isBot()) {
            return false;
        }

        long guildId = -1;
        if (event.getChannelType() == ChannelType.TEXT) {
            guildId = event.getGuild().getIdLong();
        }
        boolean processed = false;
        if (this.completeKey(new KeyPair(guildId, event.getChannel().getIdLong(), user.getIdLong()), event.getMessage())) {
            processed = true;
        }
        if (this.completeKey(new KeyPair(guildId, event.getChannel().getIdLong(), -1), event.getMessage())) {
            processed = true;
        }
        return processed;
    }

    private boolean completeKey(KeyPair keyPair, Message message) {
        Set<FuturePair> futures;
        synchronized (this.pending) {
            futures = this.pending.removeAll(keyPair);
        }
        boolean processed = false;
        for (FuturePair futurePair : futures) {
            futurePair.getFuture().complete(message);
            processed = true;
        }
        return processed;
    }

    @Data
    private static class KeyPair {
        private final long guildId, channelId, userId;
    }

    @Data
    private static class FuturePair {

        private final CompletableFuture<Message> future;
        private final long start;
        private final long expiration;

    }

}
