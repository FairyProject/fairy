/*
 * MIT License
 *
 * Copyright (c) 2022 Fairy Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.fairyproject.bukkit.mc.operator;

import io.fairyproject.bukkit.nms.BukkitNMSManager;
import io.fairyproject.bukkit.reflection.resolver.FieldResolver;
import io.fairyproject.mc.MCAdventure;
import io.fairyproject.mc.MCGameProfile;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.util.AccessUtil;
import io.fairyproject.util.exceptionally.ThrowingSupplier;
import io.fairyproject.util.filter.FilterUnit;
import io.netty.channel.Channel;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Function;

@SuppressWarnings({"deprecation"})
public class BukkitMCPlayerOperatorImpl implements BukkitMCPlayerOperator {

    private final BukkitNMSManager nmsManager;

    private final Function<Player, Integer> getPing;
    private final Function<Player, Component> getDisplayName;
    private final BiConsumer<MCPlayer, Component> setDisplayName;
    private final Function<Player, MCGameProfile> getGameProfile;

    private Method getHandle;
    private Field playerConnection;
    private Field networkManager;
    private Method profile;
    private Field ping;
    private Field channel;

    public BukkitMCPlayerOperatorImpl(BukkitNMSManager nmsManager) {
        this.nmsManager = nmsManager;

        try {
            getPing = this.setupGetPing();
            getGameProfile = this.setupGetGameProfile();
            getDisplayName = this.setupGetDisplayName();
            setDisplayName = this.setupSetDisplayName();
            this.setupGetChannel();

        } catch (Throwable e) {
            throw new IllegalStateException("Failed to initialize BukkitMCPlayerOperator.", e);
        }
    }

    private BiConsumer<MCPlayer, Component> setupSetDisplayName() {
        final BiConsumer<MCPlayer, Component> setDisplayName;
        setDisplayName = FilterUnit.<BiConsumer<MCPlayer, Component>>create()
                .add((player, component) -> player.as(Player.class).displayName(component), FilterUnit.test(t -> Player.class.getMethod("displayName", Component.class), NoSuchMethodException.class))
                .add((player, component) -> player.as(Player.class).setDisplayName(MCAdventure.asLegacyString(component, player.getLocale())))
                .find()
                .orElseThrow(() -> new IllegalStateException("No valid set display name can be found."));
        return setDisplayName;
    }

    private Function<Player, Component> setupGetDisplayName() {
        final Function<Player, Component> getDisplayName;
        getDisplayName = FilterUnit.<Function<Player, Component>>create()
                .add(player -> player.displayName(), FilterUnit.test(t -> Player.class.getMethod("displayName"), NoSuchMethodException.class))
                .add(player -> MCAdventure.LEGACY.deserialize(player.getDisplayName()))
                .find()
                .orElseThrow(() -> new IllegalStateException("No valid get display name can be found."));
        return getDisplayName;
    }

    private Function<Player, MCGameProfile> setupGetGameProfile() {
        final Function<Player, MCGameProfile> getGameProfile;
        getGameProfile = FilterUnit.<Function<Player, MCGameProfile>>create()
                .add(player -> new io.fairyproject.bukkit.mc.PaperMCGameProfile(player.getPlayerProfile()), FilterUnit.test(t -> Player.class.getMethod("getPlayerProfile"), NoSuchMethodException.class))
                .add(player -> ThrowingSupplier.sneaky(() -> MCGameProfile.from(profile.invoke(player))).get())
                .find()
                .orElseThrow(() -> new IllegalStateException("No valid get game profile can be found."));
        return getGameProfile;
    }

    private Function<Player, Integer> setupGetPing() {
        final Function<Player, Integer> getPing;
        getPing = FilterUnit.<Function<Player, Integer>>create()
                .add(player -> player.getPing(), FilterUnit.test(t -> Player.class.getMethod("getPing"), NoSuchMethodException.class))
                .add(player -> ThrowingSupplier.sneaky(() -> ping.getInt(getHandle.invoke(player))).get())
                .find()
                .orElseThrow(() -> new IllegalStateException("No valid get ping can be found."));
        return getPing;
    }

    private void setupGetChannel() throws ReflectiveOperationException {
        Class<?> craftPlayerClass = nmsManager.getObcClassResolver().resolve("entity.CraftPlayer");
        Class<?> entityPlayerClass = nmsManager.getNmsClassResolver().resolve("server.level.EntityPlayer", "EntityPlayer");
        Class<?> playerConnectionClass = nmsManager.getNmsClassResolver().resolve("server.network.PlayerConnection", "PlayerConnection");
        Class<?> networkManagerClass = nmsManager.getNmsClassResolver().resolve("network.NetworkManager", "NetworkManager");

        getHandle = craftPlayerClass.getDeclaredMethod("getHandle");
        playerConnection = new FieldResolver(entityPlayerClass).resolveByFirstType(playerConnectionClass);
        networkManager = new FieldResolver(playerConnectionClass).resolveByFirstType(networkManagerClass);
        profile = craftPlayerClass.getDeclaredMethod("getProfile");
        Field ping;
        try {
            ping = entityPlayerClass.getDeclaredField("ping");
            AccessUtil.setAccessible(ping);
        } catch (Throwable throwable) {
            ping = null;
        }
        this.ping = ping;
        channel = new FieldResolver(networkManagerClass).resolveByFirstType(Channel.class);

        AccessUtil.setAccessible(getHandle);
        AccessUtil.setAccessible(playerConnection);
        AccessUtil.setAccessible(networkManager);
        AccessUtil.setAccessible(profile);
        AccessUtil.setAccessible(channel);
    }

    @Override
    public int getPing(Player player) {
        return this.getPing.apply(player);
    }

    @Override
    public MCGameProfile getGameProfile(Player player) {
        return this.getGameProfile.apply(player);
    }

    @Override
    public Component getDisplayName(Player player) {
        return this.getDisplayName.apply(player);
    }

    @Override
    public void setDisplayName(MCPlayer player, Component displayName) {
        this.setDisplayName.accept(player, displayName);
    }

    @Override
    public Channel getChannel(Player player) {
        return ThrowingSupplier.sneaky(() -> {
            Object handle = getHandle.invoke(player);
            Object connection = playerConnection.get(handle);
            Object manager = networkManager.get(connection);
            return (Channel) channel.get(manager);
        }).get();
    }
}
