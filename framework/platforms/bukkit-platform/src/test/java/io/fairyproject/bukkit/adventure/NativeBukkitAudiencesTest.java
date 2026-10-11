package io.fairyproject.bukkit.adventure;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NativeBukkitAudiencesTest {
    private Server server;
    private ConsoleCommandSender console;
    private BukkitAudiences fallback;
    private NativeBukkitAudiences audiences;

    @BeforeEach
    void setup() {
        server = mock(Server.class);
        console = mock(ConsoleCommandSender.class);
        fallback = mock(BukkitAudiences.class);
        when(server.getConsoleSender()).thenReturn(console);
        when(server.getOnlinePlayers()).thenReturn(Collections.emptyList());
        audiences = new NativeBukkitAudiences(server, fallback);
    }

    @Test
    void messagesUseNativeConsole() {
        Component message = Component.text("native console");
        assertSame(console, audiences.console());
        audiences.all().sendMessage(message);
        verify(console).sendMessage(message);
        verifyNoInteractions(fallback);
    }

    @Test
    void cachedAudienceIncludesPlayersWhoJoinLater() {
        Audience players = audiences.players();
        Player player = mock(Player.class);
        doReturn(Collections.singletonList(player)).when(server).getOnlinePlayers();
        Component message = Component.text("joined later");
        players.sendMessage(message);
        verify(player).sendMessage(message);
        verify(console, never()).sendMessage(message);
    }

    @Test
    void worldAndPermissionFiltersExcludeOtherViewers() {
        Player player = mock(Player.class);
        World world = mock(World.class);
        when(player.getWorld()).thenReturn(world);
        when(world.getName()).thenReturn("world");
        when(player.hasPermission("fairy.test")).thenReturn(true);
        doReturn(Collections.singletonList(player)).when(server).getOnlinePlayers();
        Component message = Component.text("filtered");
        audiences.world(Key.key("other_world")).sendMessage(message);
        verify(player, never()).sendMessage(message);
        audiences.world(Key.key("world")).sendMessage(message);
        audiences.permission("fairy.test").sendMessage(message);
        verify(player, times(2)).sendMessage(message);
        verify(console, never()).sendMessage(message);
    }

    @Test
    void closedProviderStopsCachedAudiences() {
        Audience all = audiences.all();
        audiences.close();
        all.sendMessage(Component.text("closed"));
        verify(console, never()).sendMessage(any(Component.class));
        verify(fallback).close();
        assertSame(Audience.empty(), audiences.console());
    }
}
