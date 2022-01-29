package io.fairyproject.discord.proxies;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.components.ComponentLayout;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.pagination.MessagePaginationAction;
import net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Formatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ProxyMessageChannel extends MessageChannel {

    MessageChannel original();

    @Nonnull
    default String getId() {
        return original().getId();
    }

    @Nonnull
    default OffsetDateTime getTimeCreated() {
        return original().getTimeCreated();
    }

    @Nonnull
    default List<CompletableFuture<Void>> purgeMessagesById(@NotNull List<String> messageIds) {
        return original().purgeMessagesById(messageIds);
    }

    @Nonnull
    default List<CompletableFuture<Void>> purgeMessagesById(@NotNull String... messageIds) {
        return original().purgeMessagesById(messageIds);
    }

    @Nonnull
    default List<CompletableFuture<Void>> purgeMessages(@NotNull Message... messages) {
        return original().purgeMessages(messages);
    }

    @Nonnull
    default List<CompletableFuture<Void>> purgeMessages(@NotNull List<? extends Message> messages) {
        return original().purgeMessages(messages);
    }

    @Nonnull
    default List<CompletableFuture<Void>> purgeMessagesById(@NotNull long... messageIds) {
        return original().purgeMessagesById(messageIds);
    }

    @Nonnull
    default MessageAction sendMessage(@NotNull CharSequence text) {
        return original().sendMessage(text);
    }

    @Nonnull
    default MessageAction sendMessageFormat(@NotNull String format, @NotNull Object... args) {
        return original().sendMessageFormat(format, args);
    }

    @Nonnull
    default MessageAction sendMessage(@NotNull MessageEmbed embed) {
        return original().sendMessage(embed);
    }

    @Nonnull
    default MessageAction sendMessageEmbeds(@NotNull MessageEmbed embed, @NotNull MessageEmbed... other) {
        return original().sendMessageEmbeds(embed, other);
    }

    @Nonnull
    default MessageAction sendMessageEmbeds(@NotNull Collection<? extends MessageEmbed> embeds) {
        return original().sendMessageEmbeds(embeds);
    }

    @Nonnull
    default MessageAction sendMessage(@NotNull Message msg) {
        return original().sendMessage(msg);
    }

    @Nonnull
    default MessageAction sendFile(@NotNull File file, @NotNull AttachmentOption... options) {
        return original().sendFile(file, options);
    }

    @Nonnull
    default MessageAction sendFile(@NotNull File file, @NotNull String fileName, @NotNull AttachmentOption... options) {
        return original().sendFile(file, fileName, options);
    }

    @Nonnull
    default MessageAction sendFile(@NotNull InputStream data, @NotNull String fileName, @NotNull AttachmentOption... options) {
        return original().sendFile(data, fileName, options);
    }

    @Nonnull
    default MessageAction sendFile(@NotNull byte[] data, @NotNull String fileName, @NotNull AttachmentOption... options) {
        return original().sendFile(data, fileName, options);
    }

    @Nonnull
    default RestAction<Message> retrieveMessageById(@NotNull String messageId) {
        return original().retrieveMessageById(messageId);
    }

    @Nonnull
    default RestAction<Message> retrieveMessageById(long messageId) {
        return original().retrieveMessageById(messageId);
    }

    @Nonnull
    default AuditableRestAction<Void> deleteMessageById(@NotNull String messageId) {
        return original().deleteMessageById(messageId);
    }

    @Nonnull
    default AuditableRestAction<Void> deleteMessageById(long messageId) {
        return original().deleteMessageById(messageId);
    }

    default MessageHistory getHistory() {
        return original().getHistory();
    }

    @Nonnull
    default MessagePaginationAction getIterableHistory() {
        return original().getIterableHistory();
    }

    @Nonnull
    default MessageHistory.MessageRetrieveAction getHistoryAround(@NotNull String messageId, int limit) {
        return original().getHistoryAround(messageId, limit);
    }

    @Nonnull
    default MessageHistory.MessageRetrieveAction getHistoryAround(long messageId, int limit) {
        return original().getHistoryAround(messageId, limit);
    }

    @Nonnull
    default MessageHistory.MessageRetrieveAction getHistoryAround(@NotNull Message message, int limit) {
        return original().getHistoryAround(message, limit);
    }

    @Nonnull
    default MessageHistory.MessageRetrieveAction getHistoryAfter(@NotNull String messageId, int limit) {
        return original().getHistoryAfter(messageId, limit);
    }

    @Nonnull
    default MessageHistory.MessageRetrieveAction getHistoryAfter(long messageId, int limit) {
        return original().getHistoryAfter(messageId, limit);
    }

    @Nonnull
    default MessageHistory.MessageRetrieveAction getHistoryAfter(@NotNull Message message, int limit) {
        return original().getHistoryAfter(message, limit);
    }

    @Nonnull
    default MessageHistory.MessageRetrieveAction getHistoryBefore(@NotNull String messageId, int limit) {
        return original().getHistoryBefore(messageId, limit);
    }

    @Nonnull
    default MessageHistory.MessageRetrieveAction getHistoryBefore(long messageId, int limit) {
        return original().getHistoryBefore(messageId, limit);
    }

    @Nonnull
    default MessageHistory.MessageRetrieveAction getHistoryBefore(@NotNull Message message, int limit) {
        return original().getHistoryBefore(message, limit);
    }

    @Nonnull
    default MessageHistory.MessageRetrieveAction getHistoryFromBeginning(int limit) {
        return original().getHistoryFromBeginning(limit);
    }

    @Nonnull
    default RestAction<Void> sendTyping() {
        return original().sendTyping();
    }

    @Nonnull
    default RestAction<Void> addReactionById(@NotNull String messageId, @NotNull String unicode) {
        return original().addReactionById(messageId, unicode);
    }

    @Nonnull
    default RestAction<Void> addReactionById(long messageId, @NotNull String unicode) {
        return original().addReactionById(messageId, unicode);
    }

    @Nonnull
    default RestAction<Void> addReactionById(@NotNull String messageId, @NotNull Emote emote) {
        return original().addReactionById(messageId, emote);
    }

    @Nonnull
    default RestAction<Void> addReactionById(long messageId, @NotNull Emote emote) {
        return original().addReactionById(messageId, emote);
    }

    @Nonnull
    default RestAction<Void> removeReactionById(@NotNull String messageId, @NotNull String unicode) {
        return original().removeReactionById(messageId, unicode);
    }

    @Nonnull
    default RestAction<Void> removeReactionById(long messageId, @NotNull String unicode) {
        return original().removeReactionById(messageId, unicode);
    }

    @Nonnull
    default RestAction<Void> removeReactionById(@NotNull String messageId, @NotNull Emote emote) {
        return original().removeReactionById(messageId, emote);
    }

    @Nonnull
    default RestAction<Void> removeReactionById(long messageId, @NotNull Emote emote) {
        return original().removeReactionById(messageId, emote);
    }

    @Nonnull
    default ReactionPaginationAction retrieveReactionUsersById(@NotNull String messageId, @NotNull String unicode) {
        return original().retrieveReactionUsersById(messageId, unicode);
    }

    @Nonnull
    default ReactionPaginationAction retrieveReactionUsersById(long messageId, @NotNull String unicode) {
        return original().retrieveReactionUsersById(messageId, unicode);
    }

    @Nonnull
    default ReactionPaginationAction retrieveReactionUsersById(@NotNull String messageId, @NotNull Emote emote) {
        return original().retrieveReactionUsersById(messageId, emote);
    }

    @Nonnull
    default ReactionPaginationAction retrieveReactionUsersById(long messageId, @NotNull Emote emote) {
        return original().retrieveReactionUsersById(messageId, emote);
    }

    @Nonnull
    default RestAction<Void> pinMessageById(@NotNull String messageId) {
        return original().pinMessageById(messageId);
    }

    @Nonnull
    default RestAction<Void> pinMessageById(long messageId) {
        return original().pinMessageById(messageId);
    }

    @Nonnull
    default RestAction<Void> unpinMessageById(@NotNull String messageId) {
        return original().unpinMessageById(messageId);
    }

    @Nonnull
    default RestAction<Void> unpinMessageById(long messageId) {
        return original().unpinMessageById(messageId);
    }

    @Nonnull
    default RestAction<List<Message>> retrievePinnedMessages() {
        return original().retrievePinnedMessages();
    }

    @Nonnull
    default MessageAction editMessageById(@NotNull String messageId, @NotNull CharSequence newContent) {
        return original().editMessageById(messageId, newContent);
    }

    @Nonnull
    default MessageAction editMessageById(long messageId, @NotNull CharSequence newContent) {
        return original().editMessageById(messageId, newContent);
    }

    @Nonnull
    default MessageAction editMessageById(@NotNull String messageId, @NotNull Message newContent) {
        return original().editMessageById(messageId, newContent);
    }

    @Nonnull
    default MessageAction editMessageById(long messageId, @NotNull Message newContent) {
        return original().editMessageById(messageId, newContent);
    }

    @Nonnull
    default MessageAction editMessageFormatById(@NotNull String messageId, @NotNull String format, @NotNull Object... args) {
        return original().editMessageFormatById(messageId, format, args);
    }

    @Nonnull
    default MessageAction editMessageFormatById(long messageId, @NotNull String format, @NotNull Object... args) {
        return original().editMessageFormatById(messageId, format, args);
    }

    @Nonnull
    default MessageAction editMessageById(@NotNull String messageId, @NotNull MessageEmbed newEmbed) {
        return original().editMessageById(messageId, newEmbed);
    }

    @Nonnull
    default MessageAction editMessageById(long messageId, @NotNull MessageEmbed newEmbed) {
        return original().editMessageById(messageId, newEmbed);
    }

    @Nonnull
    default MessageAction editMessageEmbedsById(@NotNull String messageId, @NotNull MessageEmbed... newEmbeds) {
        return original().editMessageEmbedsById(messageId, newEmbeds);
    }

    @Nonnull
    default MessageAction editMessageEmbedsById(long messageId, @NotNull MessageEmbed... newEmbeds) {
        return original().editMessageEmbedsById(messageId, newEmbeds);
    }

    @Nonnull
    default MessageAction editMessageEmbedsById(@NotNull String messageId, @NotNull Collection<? extends MessageEmbed> newEmbeds) {
        return original().editMessageEmbedsById(messageId, newEmbeds);
    }

    @Nonnull
    default MessageAction editMessageEmbedsById(long messageId, @NotNull Collection<? extends MessageEmbed> newEmbeds) {
        return original().editMessageEmbedsById(messageId, newEmbeds);
    }

    @Nonnull
    default MessageAction editMessageComponentsById(@NotNull String messageId, @NotNull Collection<? extends ComponentLayout> components) {
        return original().editMessageComponentsById(messageId, components);
    }

    @Nonnull
    default MessageAction editMessageComponentsById(long messageId, @NotNull Collection<? extends ComponentLayout> components) {
        return original().editMessageComponentsById(messageId, components);
    }

    @Nonnull
    default MessageAction editMessageComponentsById(@NotNull String messageId, @NotNull ComponentLayout... components) {
        return original().editMessageComponentsById(messageId, components);
    }

    @Nonnull
    default MessageAction editMessageComponentsById(long messageId, @NotNull ComponentLayout... components) {
        return original().editMessageComponentsById(messageId, components);
    }

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

    default void formatTo(Formatter formatter, int i, int i1, int i2) {
        ProxyMessageChannel.this.formatTo(formatter, i, i1, i2);
    }
}
