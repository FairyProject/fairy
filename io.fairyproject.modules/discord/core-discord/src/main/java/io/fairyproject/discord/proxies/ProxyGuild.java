package io.fairyproject.discord.proxies;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Region;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.templates.Template;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.managers.GuildManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.*;
import net.dv8tion.jda.api.requests.restaction.order.CategoryOrderAction;
import net.dv8tion.jda.api.requests.restaction.order.ChannelOrderAction;
import net.dv8tion.jda.api.requests.restaction.order.RoleOrderAction;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;
import net.dv8tion.jda.api.utils.cache.MemberCacheView;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;
import net.dv8tion.jda.api.utils.cache.SortedSnowflakeCacheView;
import net.dv8tion.jda.api.utils.concurrent.Task;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface ProxyGuild extends Guild {

    Guild original();

    @NotNull
    @Override
    default RestAction<List<Command>> retrieveCommands() {
        return this.original().retrieveCommands();
    }

    @NotNull
    @Override
    default RestAction<Command> retrieveCommandById(@NotNull String id) {
        return this.original().retrieveCommandById(id);
    }

    @NotNull
    @Override
    default CommandCreateAction upsertCommand(@NotNull CommandData command) {
        return this.original().upsertCommand(command);
    }

    @NotNull
    @Override
    default CommandListUpdateAction updateCommands() {
        return this.original().updateCommands();
    }

    @NotNull
    @Override
    default CommandEditAction editCommandById(@NotNull String id) {
        return this.original().editCommandById(id);
    }

    @NotNull
    @Override
    default RestAction<Void> deleteCommandById(@NotNull String commandId) {
        return this.original().deleteCommandById(commandId);
    }

    @NotNull
    @Override
    default RestAction<List<CommandPrivilege>> retrieveCommandPrivilegesById(@NotNull String commandId) {
        return this.original().retrieveCommandPrivilegesById(commandId);
    }

    @NotNull
    @Override
    default RestAction<Map<String, List<CommandPrivilege>>> retrieveCommandPrivileges() {
        return this.original().retrieveCommandPrivileges();
    }

    @NotNull
    @Override
    default RestAction<List<CommandPrivilege>> updateCommandPrivilegesById(@NotNull String id, @NotNull Collection<? extends CommandPrivilege> privileges) {
        return this.original().updateCommandPrivilegesById(id, privileges);
    }

    @NotNull
    @Override
    default RestAction<Map<String, List<CommandPrivilege>>> updateCommandPrivileges(@NotNull Map<String, Collection<? extends CommandPrivilege>> privileges) {
        return this.original().updateCommandPrivileges(privileges);
    }

    @NotNull
    @Override
    default RestAction<EnumSet<Region>> retrieveRegions(boolean includeDeprecated) {
        return this.original().retrieveRegions(includeDeprecated);
    }

    @NotNull
    @Override
    default MemberAction addMember(@NotNull String accessToken, @NotNull String userId) {
        return this.original().addMember(accessToken, userId);
    }

    @Override
    default boolean isLoaded() {
        return this.original().isLoaded();
    }

    @Override
    default void pruneMemberCache() {
        this.original().pruneMemberCache();
    }

    @Override
    default boolean unloadMember(long userId) {
        return this.original().unloadMember(userId);
    }

    @Override
    default int getMemberCount() {
        return this.original().getMemberCount();
    }

    @NotNull
    @Override
    default String getName() {
        return this.original().getName();
    }

    @Nullable
    @Override
    default String getIconId() {
        return this.original().getIconId();
    }

    @NotNull
    @Override
    default Set<String> getFeatures() {
        return this.original().getFeatures();
    }

    @Nullable
    @Override
    default String getSplashId() {
        return this.original().getSplashId();
    }

    @NotNull
    @Override
    default RestAction<String> retrieveVanityUrl() {
        return this.original().retrieveVanityUrl();
    }

    @Nullable
    @Override
    default String getVanityCode() {
        return this.original().getVanityCode();
    }

    @NotNull
    @Override
    default RestAction<VanityInvite> retrieveVanityInvite() {
        return this.original().retrieveVanityInvite();
    }

    @Nullable
    @Override
    default String getDescription() {
        return this.original().getDescription();
    }

    @NotNull
    @Override
    default Locale getLocale() {
        return this.original().getLocale();
    }

    @Nullable
    @Override
    default String getBannerId() {
        return this.original().getBannerId();
    }

    @NotNull
    @Override
    default BoostTier getBoostTier() {
        return this.original().getBoostTier();
    }

    @Override
    default int getBoostCount() {
        return this.original().getBoostCount();
    }

    @NotNull
    @Override
    default List<Member> getBoosters() {
        return this.original().getBoosters();
    }

    @Override
    default int getMaxMembers() {
        return this.original().getMaxMembers();
    }

    @Override
    default int getMaxPresences() {
        return this.original().getMaxPresences();
    }

    @NotNull
    @Override
    default RestAction<MetaData> retrieveMetaData() {
        return this.original().retrieveMetaData();
    }

    @Nullable
    @Override
    default VoiceChannel getAfkChannel() {
        return this.original().getAfkChannel();
    }

    @Nullable
    @Override
    default TextChannel getSystemChannel() {
        return this.original().getSystemChannel();
    }

    @Nullable
    @Override
    default TextChannel getRulesChannel() {
        return this.original().getRulesChannel();
    }

    @Nullable
    @Override
    default TextChannel getCommunityUpdatesChannel() {
        return this.original().getCommunityUpdatesChannel();
    }

    @Nullable
    @Override
    default Member getOwner() {
        return this.original().getOwner();
    }

    @Override
    default long getOwnerIdLong() {
        return this.original().getOwnerIdLong();
    }

    @NotNull
    @Override
    default Timeout getAfkTimeout() {
        return this.original().getAfkTimeout();
    }

    @NotNull
    @Override
    default String getRegionRaw() {
        return this.original().getRegionRaw();
    }

    @Override
    default boolean isMember(@NotNull User user) {
        return this.original().isMember(user);
    }

    @NotNull
    @Override
    default Member getSelfMember() {
        return this.original().getSelfMember();
    }

    @NotNull
    @Override
    default NSFWLevel getNSFWLevel() {
        return this.original().getNSFWLevel();
    }

    @Nullable
    @Override
    default Member getMember(@NotNull User user) {
        return this.original().getMember(user);
    }

    @NotNull
    @Override
    default MemberCacheView getMemberCache() {
        return this.original().getMemberCache();
    }

    @NotNull
    @Override
    default SortedSnowflakeCacheView<Category> getCategoryCache() {
        return this.original().getCategoryCache();
    }

    @NotNull
    @Override
    default SortedSnowflakeCacheView<StoreChannel> getStoreChannelCache() {
        return this.original().getStoreChannelCache();
    }

    @NotNull
    @Override
    default SortedSnowflakeCacheView<TextChannel> getTextChannelCache() {
        return this.original().getTextChannelCache();
    }

    @NotNull
    @Override
    default SortedSnowflakeCacheView<VoiceChannel> getVoiceChannelCache() {
        return this.original().getVoiceChannelCache();
    }

    @NotNull
    @Override
    default List<GuildChannel> getChannels(boolean includeHidden) {
        return this.original().getChannels(includeHidden);
    }

    @NotNull
    @Override
    default SortedSnowflakeCacheView<Role> getRoleCache() {
        return this.original().getRoleCache();
    }

    @NotNull
    @Override
    default SnowflakeCacheView<Emote> getEmoteCache() {
        return this.original().getEmoteCache();
    }

    @NotNull
    @Override
    default RestAction<List<ListedEmote>> retrieveEmotes() {
        return this.original().retrieveEmotes();
    }

    @NotNull
    @Override
    default RestAction<ListedEmote> retrieveEmoteById(@NotNull String id) {
        return this.original().retrieveEmoteById(id);
    }

    @NotNull
    @Override
    default RestAction<List<Ban>> retrieveBanList() {
        return this.original().retrieveBanList();
    }

    @NotNull
    @Override
    default RestAction<Ban> retrieveBanById(@NotNull String userId) {
        return this.original().retrieveBanById(userId);
    }

    @NotNull
    @Override
    default RestAction<Integer> retrievePrunableMemberCount(int days) {
        return this.original().retrievePrunableMemberCount(days);
    }

    @NotNull
    @Override
    default Role getPublicRole() {
        return this.original().getPublicRole();
    }

    @Nullable
    @Override
    default TextChannel getDefaultChannel() {
        return this.original().getDefaultChannel();
    }

    @NotNull
    @Override
    default GuildManager getManager() {
        return this.original().getManager();
    }

    @NotNull
    @Override
    default AuditLogPaginationAction retrieveAuditLogs() {
        return this.original().retrieveAuditLogs();
    }

    @NotNull
    @Override
    default RestAction<Void> leave() {
        return this.original().leave();
    }

    @NotNull
    @Override
    default RestAction<Void> delete() {
        return this.original().delete();
    }

    @NotNull
    @Override
    default RestAction<Void> delete(@Nullable String mfaCode) {
        return this.original().delete(mfaCode);
    }

    @NotNull
    @Override
    default AudioManager getAudioManager() {
        return this.original().getAudioManager();
    }

    @NotNull
    @Override
    default Task<Void> requestToSpeak() {
        return this.original().requestToSpeak();
    }

    @NotNull
    @Override
    default Task<Void> cancelRequestToSpeak() {
        return this.original().cancelRequestToSpeak();
    }

    @NotNull
    @Override
    default JDA getJDA() {
        return this.original().getJDA();
    }

    @NotNull
    @Override
    default RestAction<List<Invite>> retrieveInvites() {
        return this.original().retrieveInvites();
    }

    @NotNull
    @Override
    default RestAction<List<Template>> retrieveTemplates() {
        return this.original().retrieveTemplates();
    }

    @NotNull
    @Override
    default RestAction<Template> createTemplate(@NotNull String name, @Nullable String description) {
        return this.original().createTemplate(name, description);
    }

    @NotNull
    @Override
    default RestAction<List<Webhook>> retrieveWebhooks() {
        return this.original().retrieveWebhooks();
    }

    @NotNull
    @Override
    default List<GuildVoiceState> getVoiceStates() {
        return this.original().getVoiceStates();
    }

    @NotNull
    @Override
    default VerificationLevel getVerificationLevel() {
        return this.original().getVerificationLevel();
    }

    @NotNull
    @Override
    default NotificationLevel getDefaultNotificationLevel() {
        return this.original().getDefaultNotificationLevel();
    }

    @NotNull
    @Override
    default MFALevel getRequiredMFALevel() {
        return this.original().getRequiredMFALevel();
    }

    @NotNull
    @Override
    default ExplicitContentLevel getExplicitContentLevel() {
        return this.original().getExplicitContentLevel();
    }

    @Override
    default boolean checkVerification() {
        return this.original().checkVerification();
    }

    @Override
    default boolean isAvailable() {
        return this.original().isAvailable();
    }

    @NotNull
    @Override
    default CompletableFuture<Void> retrieveMembers() {
        return this.original().retrieveMembers();
    }

    @NotNull
    @Override
    default Task<Void> loadMembers(@NotNull Consumer<Member> callback) {
        return this.original().loadMembers(callback);
    }

    @NotNull
    @Override
    default RestAction<Member> retrieveMemberById(long id, boolean update) {
        return this.original().retrieveMemberById(id, update);
    }

    @NotNull
    @Override
    default Task<List<Member>> retrieveMembersByIds(boolean includePresence, @NotNull long... ids) {
        return this.original().retrieveMembersByIds(includePresence, ids);
    }

    @NotNull
    @Override
    default Task<List<Member>> retrieveMembersByPrefix(@NotNull String prefix, int limit) {
        return this.original().retrieveMembersByPrefix(prefix, limit);
    }

    @NotNull
    @Override
    default RestAction<Void> moveVoiceMember(@NotNull Member member, @Nullable VoiceChannel voiceChannel) {
        return this.original().moveVoiceMember(member, voiceChannel);
    }

    @NotNull
    @Override
    default AuditableRestAction<Void> modifyNickname(@NotNull Member member, @Nullable String nickname) {
        return this.original().modifyNickname(member, nickname);
    }

    @NotNull
    @Override
    default AuditableRestAction<Integer> prune(int days, boolean wait, @NotNull Role... roles) {
        return this.original().prune(days, wait, roles);
    }

    @NotNull
    @Override
    default AuditableRestAction<Void> kick(@NotNull Member member, @Nullable String reason) {
        return this.original().kick(member, reason);
    }

    @NotNull
    @Override
    default AuditableRestAction<Void> kick(@NotNull String userId, @Nullable String reason) {
        return this.original().kick(userId, reason);
    }

    @NotNull
    @Override
    default AuditableRestAction<Void> ban(@NotNull User user, int delDays, @Nullable String reason) {
        return this.original().ban(user, delDays, reason);
    }

    @NotNull
    @Override
    default AuditableRestAction<Void> ban(@NotNull String userId, int delDays, @Nullable String reason) {
        return this.original().ban(userId, delDays, reason);
    }

    @NotNull
    @Override
    default AuditableRestAction<Void> unban(@NotNull String userId) {
        return this.original().unban(userId);
    }

    @NotNull
    @Override
    default AuditableRestAction<Void> deafen(@NotNull Member member, boolean deafen) {
        return this.original().deafen(member, deafen);
    }

    @NotNull
    @Override
    default AuditableRestAction<Void> mute(@NotNull Member member, boolean mute) {
        return this.original().mute(member, mute);
    }

    @NotNull
    @Override
    default AuditableRestAction<Void> addRoleToMember(@NotNull Member member, @NotNull Role role) {
        return this.original().addRoleToMember(member, role);
    }

    @NotNull
    @Override
    default AuditableRestAction<Void> removeRoleFromMember(@NotNull Member member, @NotNull Role role) {
        return this.original().removeRoleFromMember(member, role);
    }

    @NotNull
    @Override
    default AuditableRestAction<Void> modifyMemberRoles(@NotNull Member member, @Nullable Collection<Role> rolesToAdd, @Nullable Collection<Role> rolesToRemove) {
        return this.original().modifyMemberRoles(member, rolesToAdd, rolesToRemove);
    }

    @NotNull
    @Override
    default AuditableRestAction<Void> modifyMemberRoles(@NotNull Member member, @NotNull Collection<Role> roles) {
        return this.original().modifyMemberRoles(member, roles);
    }

    @NotNull
    @Override
    default AuditableRestAction<Void> transferOwnership(@NotNull Member newOwner) {
        return this.original().transferOwnership(newOwner);
    }

    @NotNull
    @Override
    default ChannelAction<TextChannel> createTextChannel(@NotNull String name, @Nullable Category parent) {
        return this.original().createTextChannel(name, parent);
    }

    @NotNull
    @Override
    default ChannelAction<VoiceChannel> createVoiceChannel(@NotNull String name, @Nullable Category parent) {
        return this.original().createVoiceChannel(name, parent);
    }

    @NotNull
    @Override
    default ChannelAction<StageChannel> createStageChannel(@NotNull String name, @Nullable Category parent) {
        return this.original().createStageChannel(name, parent);
    }

    @NotNull
    @Override
    default ChannelAction<Category> createCategory(@NotNull String name) {
        return this.original().createCategory(name);
    }

    @NotNull
    @Override
    default RoleAction createRole() {
        return this.original().createRole();
    }

    @NotNull
    @Override
    default AuditableRestAction<Emote> createEmote(@NotNull String name, @NotNull Icon icon, @NotNull Role... roles) {
        return this.original().createEmote(name, icon, roles);
    }

    @NotNull
    @Override
    default ChannelOrderAction modifyCategoryPositions() {
        return this.original().modifyCategoryPositions();
    }

    @NotNull
    @Override
    default ChannelOrderAction modifyTextChannelPositions() {
        return this.original().modifyTextChannelPositions();
    }

    @NotNull
    @Override
    default ChannelOrderAction modifyVoiceChannelPositions() {
        return this.original().modifyVoiceChannelPositions();
    }

    @NotNull
    @Override
    default CategoryOrderAction modifyTextChannelPositions(@NotNull Category category) {
        return this.original().modifyTextChannelPositions(category);
    }

    @NotNull
    @Override
    default CategoryOrderAction modifyVoiceChannelPositions(@NotNull Category category) {
        return this.original().modifyVoiceChannelPositions(category);
    }

    @NotNull
    @Override
    default RoleOrderAction modifyRolePositions(boolean useAscendingOrder) {
        return this.original().modifyRolePositions(useAscendingOrder);
    }

    @Override
    default long getIdLong() {
        return this.original().getIdLong();
    }
}
