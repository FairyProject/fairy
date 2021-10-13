package org.fairy.bukkit.player;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;
import org.fairy.bukkit.reflection.MinecraftReflection;
import org.fairy.mc.MCPlayer;
import org.fairy.mc.title.MCTitle;

import java.util.Iterator;
import java.util.UUID;

public class BukkitMCPlayer implements MCPlayer {

    private final Player player;
    private final Channel channel;

    public BukkitMCPlayer(Player player) {
        this.player = player;
        this.channel = MinecraftReflection.getChannel(player);
    }

    @Override
    public UUID getUUID() {
        return this.player.getUniqueId();
    }

    @Override
    public String getName() {
        return this.player.getName();
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(message);
    }

    @Override
    public void sendMessage(String... messages) {
        player.sendMessage(messages);
    }

    @Override
    public void sendMessage(Iterable<String> messages) {
        final Iterator<String> iterator = messages.iterator();
        while (iterator.hasNext()) {
            player.sendMessage(iterator.next());
        }
    }

    @Override
    public void sendTitle(MCTitle title) {

    }

    @Override
    public Channel getChannel() {
        return this.channel;
    }

    @Override
    public <T> T as(Class<T> playerClass) {
        if (!playerClass.isInstance(this.player)) {
            throw new ClassCastException();
        }
        return playerClass.cast(this.player);
    }
}
