package io.fairyproject.discord.proxies;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.managers.DirectAudioController;
import net.dv8tion.jda.api.managers.Presence;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import net.dv8tion.jda.api.requests.restaction.CommandEditAction;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.requests.restaction.GuildAction;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.cache.CacheView;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

public interface ProxyJDA extends JDA {

    JDA getJDA();

    @NotNull
    @Override
    default Status getStatus() {
        return this.getJDA().getStatus();
    }

    @NotNull
    @Override
    default EnumSet<GatewayIntent> getGatewayIntents() {
        return this.getJDA().getGatewayIntents();
    }

    @NotNull
    @Override
    default EnumSet<CacheFlag> getCacheFlags() {
        return this.getJDA().getCacheFlags();
    }

    @Override
    default boolean unloadUser(long userId) {
        return this.getJDA().unloadUser(userId);
    }

    @Override
    default long getGatewayPing() {
        return this.getJDA().getGatewayPing();
    }

    @NotNull
    @Override
    default JDA awaitStatus(@NotNull JDA.Status status, @NotNull Status... failOn) throws InterruptedException {
        return this.getJDA().awaitStatus(status, failOn);
    }

    @Override
    default int cancelRequests() {
        return this.getJDA().cancelRequests();
    }

    @NotNull
    @Override
    default ScheduledExecutorService getRateLimitPool() {
        return this.getJDA().getRateLimitPool();
    }

    @NotNull
    @Override
    default ScheduledExecutorService getGatewayPool() {
        return this.getJDA().getGatewayPool();
    }

    @NotNull
    @Override
    default ExecutorService getCallbackPool() {
        return this.getJDA().getCallbackPool();
    }

    @NotNull
    @Override
    default OkHttpClient getHttpClient() {
        return this.getJDA().getHttpClient();
    }

    @NotNull
    @Override
    default DirectAudioController getDirectAudioController() {
        return this.getJDA().getDirectAudioController();
    }

    @Override
    default void setEventManager(@Nullable IEventManager manager) {
        this.getJDA().setEventManager(manager);
    }

    @Override
    default void addEventListener(@NotNull Object... listeners) {
        this.getJDA().addEventListener(listeners);
    }

    @Override
    default void removeEventListener(@NotNull Object... listeners) {
        this.getJDA().removeEventListener(listeners);
    }

    @NotNull
    @Override
    default List<Object> getRegisteredListeners() {
        return this.getJDA().getRegisteredListeners();
    }

    @NotNull
    @Override
    default RestAction<List<Command>> retrieveCommands() {
        return this.getJDA().retrieveCommands();
    }

    @NotNull
    @Override
    default RestAction<Command> retrieveCommandById(@NotNull String id) {
        return this.getJDA().retrieveCommandById(id);
    }

    @NotNull
    @Override
    default CommandCreateAction upsertCommand(@NotNull CommandData command) {
        return this.getJDA().upsertCommand(command);
    }

    @NotNull
    @Override
    default CommandListUpdateAction updateCommands() {
        return this.getJDA().updateCommands();
    }

    @NotNull
    @Override
    default CommandEditAction editCommandById(@NotNull String id) {
        return this.getJDA().editCommandById(id);
    }

    @NotNull
    @Override
    default RestAction<Void> deleteCommandById(@NotNull String commandId) {
        return this.getJDA().deleteCommandById(commandId);
    }

    @NotNull
    @Override
    default GuildAction createGuild(@NotNull String name) {
        return this.getJDA().createGuild(name);
    }

    @NotNull
    @Override
    default RestAction<Void> createGuildFromTemplate(@NotNull String code, @NotNull String name, @Nullable Icon icon) {
        return this.getJDA().createGuildFromTemplate(code, name, icon);
    }

    @NotNull
    @Override
    default CacheView<AudioManager> getAudioManagerCache() {
        return this.getJDA().getAudioManagerCache();
    }

    @NotNull
    @Override
    default SnowflakeCacheView<User> getUserCache() {
        return this.getJDA().getUserCache();
    }

    @NotNull
    @Override
    default List<Guild> getMutualGuilds(@NotNull User... users) {
        return this.getJDA().getMutualGuilds(users);
    }

    @NotNull
    @Override
    default List<Guild> getMutualGuilds(@NotNull Collection<User> users) {
        return this.getJDA().getMutualGuilds(users);
    }

    @NotNull
    @Override
    default RestAction<User> retrieveUserById(long id, boolean update) {
        return this.getJDA().retrieveUserById(id, update);
    }

    @NotNull
    @Override
    default SnowflakeCacheView<Guild> getGuildCache() {
        return this.getJDA().getGuildCache();
    }

    @NotNull
    @Override
    default Set<String> getUnavailableGuilds() {
        return this.getJDA().getUnavailableGuilds();
    }

    @Override
    default boolean isUnavailable(long guildId) {
        return this.getJDA().isUnavailable(guildId);
    }

    @NotNull
    @Override
    default SnowflakeCacheView<Role> getRoleCache() {
        return this.getJDA().getRoleCache();
    }

    @NotNull
    @Override
    default SnowflakeCacheView<Category> getCategoryCache() {
        return this.getJDA().getCategoryCache();
    }

    @NotNull
    @Override
    default SnowflakeCacheView<StoreChannel> getStoreChannelCache() {
        return this.getJDA().getStoreChannelCache();
    }

    @NotNull
    @Override
    default SnowflakeCacheView<TextChannel> getTextChannelCache() {
        return this.getJDA().getTextChannelCache();
    }

    @NotNull
    @Override
    default SnowflakeCacheView<VoiceChannel> getVoiceChannelCache() {
        return this.getJDA().getVoiceChannelCache();
    }

    @NotNull
    @Override
    default SnowflakeCacheView<PrivateChannel> getPrivateChannelCache() {
        return this.getJDA().getPrivateChannelCache();
    }

    @NotNull
    @Override
    default RestAction<PrivateChannel> openPrivateChannelById(long userId) {
        return this.getJDA().openPrivateChannelById(userId);
    }

    @NotNull
    @Override
    default SnowflakeCacheView<Emote> getEmoteCache() {
        return this.getJDA().getEmoteCache();
    }

    @NotNull
    @Override
    default IEventManager getEventManager() {
        return this.getJDA().getEventManager();
    }

    @NotNull
    @Override
    default SelfUser getSelfUser() {
        return this.getJDA().getSelfUser();
    }

    @NotNull
    @Override
    default Presence getPresence() {
        return this.getJDA().getPresence();
    }

    @NotNull
    @Override
    default ShardInfo getShardInfo() {
        return this.getJDA().getShardInfo();
    }

    @NotNull
    @Override
    default String getToken() {
        return this.getJDA().getToken();
    }

    @Override
    default long getResponseTotal() {
        return this.getJDA().getResponseTotal();
    }

    @Override
    default int getMaxReconnectDelay() {
        return this.getJDA().getMaxReconnectDelay();
    }

    @Override
    default void setAutoReconnect(boolean reconnect) {
        this.getJDA().setAutoReconnect(reconnect);
    }

    @Override
    default void setRequestTimeoutRetry(boolean retryOnTimeout) {
        this.getJDA().setRequestTimeoutRetry(retryOnTimeout);
    }

    @Override
    default boolean isAutoReconnect() {
        return this.getJDA().isAutoReconnect();
    }

    @Override
    default boolean isBulkDeleteSplittingEnabled() {
        return this.getJDA().isBulkDeleteSplittingEnabled();
    }

    @Override
    default void shutdown() {
        this.getJDA().shutdown();
    }

    @Override
    default void shutdownNow() {
        this.getJDA().shutdownNow();
    }

    @NotNull
    @Override
    default AccountType getAccountType() {
        return this.getJDA().getAccountType();
    }

    @NotNull
    @Override
    default RestAction<ApplicationInfo> retrieveApplicationInfo() {
        return this.getJDA().retrieveApplicationInfo();
    }

    @NotNull
    @Override
    default JDA setRequiredScopes(@NotNull Collection<String> scopes) {
        return this.getJDA().setRequiredScopes(scopes);
    }

    @NotNull
    @Override
    default String getInviteUrl(@Nullable Permission... permissions) {
        return this.getJDA().getInviteUrl(permissions);
    }

    @NotNull
    @Override
    default String getInviteUrl(@Nullable Collection<Permission> permissions) {
        return this.getJDA().getInviteUrl(permissions);
    }

    @Nullable
    @Override
    default ShardManager getShardManager() {
        return this.getJDA().getShardManager();
    }

    @NotNull
    @Override
    default RestAction<Webhook> retrieveWebhookById(@NotNull String webhookId) {
        return this.getJDA().retrieveWebhookById(webhookId);
    }
}
