package io.fairyproject.discord.proxies;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.ChannelManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface ProxyTextChannel extends TextChannel, ProxyMessageChannel {

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
}
