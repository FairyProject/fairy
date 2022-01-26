package io.fairyproject.discord.proxies;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.components.ComponentLayout;
import net.dv8tion.jda.api.managers.ChannelManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.*;
import net.dv8tion.jda.api.requests.restaction.pagination.MessagePaginationAction;
import net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Formatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ProxyTextChannel extends TextChannel {
    @NotNull
    @Override
    default RestAction<Webhook.WebhookReference> follow(long targetChannelId) {
        return this.original().follow(targetChannelId);
    }

    @NotNull
    @Override
    default RestAction<Webhook.WebhookReference> follow(@NotNull TextChannel targetChannel) {
        return this.original().follow(targetChannel);
    }

    @NotNull
    @Override
    default RestAction<Void> clearReactionsById(long messageId) {
        return this.original().clearReactionsById(messageId);
    }

    @NotNull
    @Override
    default RestAction<Void> clearReactionsById(long messageId, @NotNull String unicode) {
        return this.original().clearReactionsById(messageId, unicode);
    }

    @NotNull
    @Override
    default RestAction<Void> clearReactionsById(long messageId, @NotNull Emote emote) {
        return this.original().clearReactionsById(messageId, emote);
    }

    @NotNull
    @Override
    default RestAction<Void> removeReactionById(long messageId, @NotNull String unicode, @NotNull User user) {
        return this.original().removeReactionById(messageId, unicode, user);
    }

    @NotNull
    @Override
    default RestAction<Void> removeReactionById(@NotNull String messageId, @NotNull Emote emote, @NotNull User user) {
        return this.original().removeReactionById(messageId, emote, user);
    }

    @NotNull
    @Override
    default RestAction<Void> removeReactionById(long messageId, @NotNull Emote emote, @NotNull User user) {
        return this.original().removeReactionById(messageId, emote, user);
    }

    @NotNull
    @Override
    default RestAction<Message> crosspostMessageById(@NotNull String messageId) {
        return this.original().crosspostMessageById(messageId);
    }

    @NotNull
    @Override
    default RestAction<Message> crosspostMessageById(long messageId) {
        return this.original().crosspostMessageById(messageId);
    }

    @NotNull
    @Override
    default PermissionOverrideAction upsertPermissionOverride(@NotNull IPermissionHolder permissionHolder) {
        return this.original().upsertPermissionOverride(permissionHolder);
    }

    TextChannel original();

    @Nullable
    @Override
    default String getTopic() {
        return this.original().getTopic();
    }

    @Override
    default boolean isNSFW() {
        return this.original().isNSFW();
    }

    @Override
    default boolean isNews() {
        return this.original().isNews();
    }

    @Override
    default int getSlowmode() {
        return this.original().getSlowmode();
    }

    @NotNull
    @Override
    default Guild getGuild() {
        return this.original().getGuild();
    }

    @Nullable
    @Override
    default Category getParent() {
        return this.original().getParent();
    }

    @NotNull
    @Override
    default List<Member> getMembers() {
        return this.original().getMembers();
    }

    @Override
    default int getPosition() {
        return this.original().getPosition();
    }

    @Override
    default int getPositionRaw() {
        return this.original().getPositionRaw();
    }

    @Nullable
    @Override
    default PermissionOverride getPermissionOverride(@NotNull IPermissionHolder permissionHolder) {
        return this.original().getPermissionOverride(permissionHolder);
    }

    @NotNull
    @Override
    default List<PermissionOverride> getPermissionOverrides() {
        return this.original().getPermissionOverrides();
    }

    @NotNull
    @Override
    default List<PermissionOverride> getMemberPermissionOverrides() {
        return this.original().getMemberPermissionOverrides();
    }

    @NotNull
    @Override
    default List<PermissionOverride> getRolePermissionOverrides() {
        return this.original().getRolePermissionOverrides();
    }

    @Override
    default boolean isSynced() {
        return this.original().isSynced();
    }

    @NotNull
    @Override
    default ChannelAction<TextChannel> createCopy(@NotNull Guild guild) {
        return this.original().createCopy(guild);
    }

    @NotNull
    @Override
    default ChannelAction<TextChannel> createCopy() {
        return this.original().createCopy();
    }

    @NotNull
    @Override
    default ChannelManager getManager() {
        return this.original().getManager();
    }

    @NotNull
    @Override
    default AuditableRestAction<Void> delete() {
        return this.original().delete();
    }

    @NotNull
    @Override
    default PermissionOverrideAction createPermissionOverride(@NotNull IPermissionHolder permissionHolder) {
        return this.original().createPermissionOverride(permissionHolder);
    }

    @NotNull
    @Override
    default PermissionOverrideAction putPermissionOverride(@NotNull IPermissionHolder permissionHolder) {
        return this.original().putPermissionOverride(permissionHolder);
    }

    @NotNull
    @Override
    default InviteAction createInvite() {
        return this.original().createInvite();
    }

    @NotNull
    @Override
    default RestAction<List<Invite>> retrieveInvites() {
        return this.original().retrieveInvites();
    }

    @NotNull
    @Override
    default RestAction<List<Webhook>> retrieveWebhooks() {
        return this.original().retrieveWebhooks();
    }

    @NotNull
    @Override
    default WebhookAction createWebhook(@NotNull String name) {
        return this.original().createWebhook(name);
    }

    @NotNull
    @Override
    default RestAction<Webhook.WebhookReference> follow(@NotNull String targetChannelId) {
        return this.original().follow(targetChannelId);
    }

    @NotNull
    @Override
    default RestAction<Void> deleteMessages(@NotNull Collection<Message> messages) {
        return this.original().deleteMessages(messages);
    }

    @NotNull
    @Override
    default RestAction<Void> deleteMessagesByIds(@NotNull Collection<String> messageIds) {
        return this.original().deleteMessagesByIds(messageIds);
    }

    @NotNull
    @Override
    default AuditableRestAction<Void> deleteWebhookById(@NotNull String id) {
        return this.original().deleteWebhookById(id);
    }

    @NotNull
    @Override
    default RestAction<Void> clearReactionsById(@NotNull String messageId) {
        return this.original().clearReactionsById(messageId);
    }

    @NotNull
    @Override
    default RestAction<Void> clearReactionsById(@NotNull String messageId, @NotNull String unicode) {
        return this.original().clearReactionsById(messageId, unicode);
    }

    @NotNull
    @Override
    default RestAction<Void> clearReactionsById(@NotNull String messageId, @NotNull Emote emote) {
        return this.original().clearReactionsById(messageId, emote);
    }

    @NotNull
    @Override
    default RestAction<Void> removeReactionById(@NotNull String messageId, @NotNull String unicode, @NotNull User user) {
        return this.original().removeReactionById(messageId, unicode, user);
    }

    @Override
    default boolean canTalk() {
        return this.original().canTalk();
    }

    @Override
    default boolean canTalk(@NotNull Member member) {
        return this.original().canTalk(member);
    }

    @Override
    default int compareTo(@NotNull GuildChannel o) {
        return this.original().compareTo(o);
    }

    @NotNull
    @Override
    default String getAsMention() {
        return this.original().getAsMention();
    }

    @Nonnull
    default String getLatestMessageId() {
        return this.original().getLatestMessageId();
    }

    @Nonnull
    default List<CompletableFuture<Void>> purgeMessagesById(@NotNull List<String> messageIds) {
        return this.original().purgeMessagesById(messageIds);
    }

    @Nonnull
    default List<CompletableFuture<Void>> purgeMessagesById(@NotNull String... messageIds) {
        return this.original().purgeMessagesById(messageIds);
    }

    @Nonnull
    default List<CompletableFuture<Void>> purgeMessages(@NotNull Message... messages) {
        return this.original().purgeMessages(messages);
    }

    @Nonnull
    default List<CompletableFuture<Void>> purgeMessages(@NotNull List<? extends Message> messages) {
        return this.original().purgeMessages(messages);
    }

    @Nonnull
    default List<CompletableFuture<Void>> purgeMessagesById(@NotNull long... messageIds) {
        return this.original().purgeMessagesById(messageIds);
    }

    default long getLatestMessageIdLong() {
        return this.original().getLatestMessageIdLong();
    }

    default boolean hasLatestMessage() {
        return this.original().hasLatestMessage();
    }

    @Nonnull
    default MessageAction sendMessage(@NotNull CharSequence text) {
        return this.original().sendMessage(text);
    }

    @Nonnull
    default MessageAction sendMessageFormat(@NotNull String format, @NotNull Object... args) {
        return this.original().sendMessageFormat(format, args);
    }

    @Nonnull
    default MessageAction sendMessage(@NotNull MessageEmbed embed) {
        return this.original().sendMessage(embed);
    }

    @Nonnull
    default MessageAction sendMessageEmbeds(@NotNull MessageEmbed embed, @NotNull MessageEmbed... other) {
        return this.original().sendMessageEmbeds(embed, other);
    }

    @Nonnull
    default MessageAction sendMessageEmbeds(@NotNull Collection<? extends MessageEmbed> embeds) {
        return this.original().sendMessageEmbeds(embeds);
    }

    @Nonnull
    default MessageAction sendMessage(@NotNull Message msg) {
        return this.original().sendMessage(msg);
    }

    @Nonnull
    default MessageAction sendFile(@NotNull File file, @NotNull AttachmentOption... options) {
        return this.original().sendFile(file, options);
    }

    @Nonnull
    default MessageAction sendFile(@NotNull File file, @NotNull String fileName, @NotNull AttachmentOption... options) {
        return this.original().sendFile(file, fileName, options);
    }

    @Nonnull
    default MessageAction sendFile(@NotNull InputStream data, @NotNull String fileName, @NotNull AttachmentOption... options) {
        return this.original().sendFile(data, fileName, options);
    }

    @Nonnull
    default MessageAction sendFile(@NotNull byte[] data, @NotNull String fileName, @NotNull AttachmentOption... options) {
        return this.original().sendFile(data, fileName, options);
    }

    @Nonnull
    default RestAction<Message> retrieveMessageById(@NotNull String messageId) {
        return this.original().retrieveMessageById(messageId);
    }

    @Nonnull
    default RestAction<Message> retrieveMessageById(long messageId) {
        return this.original().retrieveMessageById(messageId);
    }

    @Nonnull
    default AuditableRestAction<Void> deleteMessageById(@NotNull String messageId) {
        return this.original().deleteMessageById(messageId);
    }

    @Nonnull
    default AuditableRestAction<Void> deleteMessageById(long messageId) {
        return this.original().deleteMessageById(messageId);
    }

    default MessageHistory getHistory() {
        return this.original().getHistory();
    }

    @Nonnull
    default MessagePaginationAction getIterableHistory() {
        return this.original().getIterableHistory();
    }

    @Nonnull
    default MessageHistory.MessageRetrieveAction getHistoryAround(@NotNull String messageId, int limit) {
        return this.original().getHistoryAround(messageId, limit);
    }

    @Nonnull
    default MessageHistory.MessageRetrieveAction getHistoryAround(long messageId, int limit) {
        return this.original().getHistoryAround(messageId, limit);
    }

    @Nonnull
    default MessageHistory.MessageRetrieveAction getHistoryAround(@NotNull Message message, int limit) {
        return this.original().getHistoryAround(message, limit);
    }

    @Nonnull
    default MessageHistory.MessageRetrieveAction getHistoryAfter(@NotNull String messageId, int limit) {
        return this.original().getHistoryAfter(messageId, limit);
    }

    @Nonnull
    default MessageHistory.MessageRetrieveAction getHistoryAfter(long messageId, int limit) {
        return this.original().getHistoryAfter(messageId, limit);
    }

    @Nonnull
    default MessageHistory.MessageRetrieveAction getHistoryAfter(@NotNull Message message, int limit) {
        return this.original().getHistoryAfter(message, limit);
    }

    @Nonnull
    default MessageHistory.MessageRetrieveAction getHistoryBefore(@NotNull String messageId, int limit) {
        return this.original().getHistoryBefore(messageId, limit);
    }

    @Nonnull
    default MessageHistory.MessageRetrieveAction getHistoryBefore(long messageId, int limit) {
        return this.original().getHistoryBefore(messageId, limit);
    }

    @Nonnull
    default MessageHistory.MessageRetrieveAction getHistoryBefore(@NotNull Message message, int limit) {
        return this.original().getHistoryBefore(message, limit);
    }

    @Nonnull
    default MessageHistory.MessageRetrieveAction getHistoryFromBeginning(int limit) {
        return this.original().getHistoryFromBeginning(limit);
    }

    @Nonnull
    default RestAction<Void> sendTyping() {
        return this.original().sendTyping();
    }

    @Nonnull
    default RestAction<Void> addReactionById(@NotNull String messageId, @NotNull String unicode) {
        return this.original().addReactionById(messageId, unicode);
    }

    @Nonnull
    default RestAction<Void> addReactionById(long messageId, @NotNull String unicode) {
        return this.original().addReactionById(messageId, unicode);
    }

    @Nonnull
    default RestAction<Void> addReactionById(@NotNull String messageId, @NotNull Emote emote) {
        return this.original().addReactionById(messageId, emote);
    }

    @Nonnull
    default RestAction<Void> addReactionById(long messageId, @NotNull Emote emote) {
        return this.original().addReactionById(messageId, emote);
    }

    @Nonnull
    default RestAction<Void> removeReactionById(@NotNull String messageId, @NotNull String unicode) {
        return this.original().removeReactionById(messageId, unicode);
    }

    @Nonnull
    default RestAction<Void> removeReactionById(long messageId, @NotNull String unicode) {
        return this.original().removeReactionById(messageId, unicode);
    }

    @Nonnull
    default RestAction<Void> removeReactionById(@NotNull String messageId, @NotNull Emote emote) {
        return this.original().removeReactionById(messageId, emote);
    }

    @Nonnull
    default RestAction<Void> removeReactionById(long messageId, @NotNull Emote emote) {
        return this.original().removeReactionById(messageId, emote);
    }

    @Nonnull
    default ReactionPaginationAction retrieveReactionUsersById(@NotNull String messageId, @NotNull String unicode) {
        return this.original().retrieveReactionUsersById(messageId, unicode);
    }

    @Nonnull
    default ReactionPaginationAction retrieveReactionUsersById(long messageId, @NotNull String unicode) {
        return this.original().retrieveReactionUsersById(messageId, unicode);
    }

    @Nonnull
    default ReactionPaginationAction retrieveReactionUsersById(@NotNull String messageId, @NotNull Emote emote) {
        return this.original().retrieveReactionUsersById(messageId, emote);
    }

    @Nonnull
    default ReactionPaginationAction retrieveReactionUsersById(long messageId, @NotNull Emote emote) {
        return this.original().retrieveReactionUsersById(messageId, emote);
    }

    @Nonnull
    default RestAction<Void> pinMessageById(@NotNull String messageId) {
        return this.original().pinMessageById(messageId);
    }

    @Nonnull
    default RestAction<Void> pinMessageById(long messageId) {
        return this.original().pinMessageById(messageId);
    }

    @Nonnull
    default RestAction<Void> unpinMessageById(@NotNull String messageId) {
        return this.original().unpinMessageById(messageId);
    }

    @Nonnull
    default RestAction<Void> unpinMessageById(long messageId) {
        return this.original().unpinMessageById(messageId);
    }

    @Nonnull
    default RestAction<List<Message>> retrievePinnedMessages() {
        return this.original().retrievePinnedMessages();
    }

    @Nonnull
    default MessageAction editMessageById(@NotNull String messageId, @NotNull CharSequence newContent) {
        return this.original().editMessageById(messageId, newContent);
    }

    @Nonnull
    default MessageAction editMessageById(long messageId, @NotNull CharSequence newContent) {
        return this.original().editMessageById(messageId, newContent);
    }

    @Nonnull
    default MessageAction editMessageById(@NotNull String messageId, @NotNull Message newContent) {
        return this.original().editMessageById(messageId, newContent);
    }

    @Nonnull
    default MessageAction editMessageById(long messageId, @NotNull Message newContent) {
        return this.original().editMessageById(messageId, newContent);
    }

    @Nonnull
    default MessageAction editMessageFormatById(@NotNull String messageId, @NotNull String format, @NotNull Object... args) {
        return this.original().editMessageFormatById(messageId, format, args);
    }

    @Nonnull
    default MessageAction editMessageFormatById(long messageId, @NotNull String format, @NotNull Object... args) {
        return this.original().editMessageFormatById(messageId, format, args);
    }

    @Nonnull
    default MessageAction editMessageById(@NotNull String messageId, @NotNull MessageEmbed newEmbed) {
        return this.original().editMessageById(messageId, newEmbed);
    }

    @Nonnull
    default MessageAction editMessageById(long messageId, @NotNull MessageEmbed newEmbed) {
        return this.original().editMessageById(messageId, newEmbed);
    }

    @Nonnull
    default MessageAction editMessageEmbedsById(@NotNull String messageId, @NotNull MessageEmbed... newEmbeds) {
        return this.original().editMessageEmbedsById(messageId, newEmbeds);
    }

    @Nonnull
    default MessageAction editMessageEmbedsById(long messageId, @NotNull MessageEmbed... newEmbeds) {
        return this.original().editMessageEmbedsById(messageId, newEmbeds);
    }

    @Nonnull
    default MessageAction editMessageEmbedsById(@NotNull String messageId, @NotNull Collection<? extends MessageEmbed> newEmbeds) {
        return this.original().editMessageEmbedsById(messageId, newEmbeds);
    }

    @Nonnull
    default MessageAction editMessageEmbedsById(long messageId, @NotNull Collection<? extends MessageEmbed> newEmbeds) {
        return this.original().editMessageEmbedsById(messageId, newEmbeds);
    }

    @Nonnull
    default MessageAction editMessageComponentsById(@NotNull String messageId, @NotNull Collection<? extends ComponentLayout> components) {
        return this.original().editMessageComponentsById(messageId, components);
    }

    @Nonnull
    default MessageAction editMessageComponentsById(long messageId, @NotNull Collection<? extends ComponentLayout> components) {
        return this.original().editMessageComponentsById(messageId, components);
    }

    @Nonnull
    default MessageAction editMessageComponentsById(@NotNull String messageId, @NotNull ComponentLayout... components) {
        return this.original().editMessageComponentsById(messageId, components);
    }

    @Nonnull
    default MessageAction editMessageComponentsById(long messageId, @NotNull ComponentLayout... components) {
        return this.original().editMessageComponentsById(messageId, components);
    }

    default void formatTo(Formatter formatter, int flags, int width, int precision) {
        this.original().formatTo(formatter, flags, width, precision);
    }

    @Nonnull
    default String getId() {
        return this.original().getId();
    }

    @Nonnull
    default OffsetDateTime getTimeCreated() {
        return this.original().getTimeCreated();
    }

    default @NotNull String getName() {
        return this.original().getName();
    }

    default @NotNull ChannelType getType() {
        return this.original().getType();
    }

    default @NotNull JDA getJDA() {
        return this.original().getJDA();
    }

    default long getIdLong() {
        return this.original().getIdLong();
    }
}
