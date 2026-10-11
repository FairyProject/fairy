package io.fairyproject.bukkit.adventure;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Uses Paper's native audiences when the server and plugin share Adventure classes.
 * Relocated Adventure and older Spigot servers retain the Bukkit adapter.
 */
public final class NativeBukkitAudiences implements BukkitAudiences {
    private final Server server;
    private final BukkitAudiences fallback;
    private volatile boolean closed;

    NativeBukkitAudiences(Server server, BukkitAudiences fallback) {
        this.server = server;
        this.fallback = fallback;
    }

    public static BukkitAudiences create(Plugin plugin) {
        Server server = plugin.getServer();
        if (server.getConsoleSender() instanceof Audience) {
            return new NativeBukkitAudiences(server, BukkitAudiences.create(plugin));
        }
        return BukkitAudiences.create(plugin);
    }

    @Override
    public Audience sender(CommandSender sender) {
        if (this.closed) {
            return Audience.empty();
        }
        return sender instanceof Audience ? (Audience) sender : this.fallback.sender(sender);
    }

    @Override
    public Audience player(Player player) {
        return this.sender(player);
    }

    @Override
    public Audience player(UUID playerId) {
        Player player = this.server.getPlayer(playerId);
        return player == null ? Audience.empty() : this.player(player);
    }

    @Override
    public Audience console() {
        return this.sender(this.server.getConsoleSender());
    }

    @Override
    public Audience all() {
        return this.filter(sender -> true);
    }

    @Override
    public Audience players() {
        return this.filter(sender -> sender instanceof Player);
    }

    @Override
    public Audience filter(Predicate<CommandSender> filter) {
        // Resolve viewers when sending, so cached audiences include later joins.
        return (ForwardingAudience) () -> {
            List<Audience> viewers = new ArrayList<>();
            if (this.closed) {
                return viewers;
            }
            CommandSender console = this.server.getConsoleSender();
            if (filter.test(console)) {
                viewers.add(this.sender(console));
            }
            for (Player player : this.server.getOnlinePlayers()) {
                if (filter.test(player)) {
                    viewers.add(this.player(player));
                }
            }
            return viewers;
        };
    }

    @Override
    public Audience permission(String permission) {
        return this.filter(sender -> sender.hasPermission(permission));
    }

    @Override
    public Audience world(Key world) {
        return this.filter(sender -> sender instanceof Player
                && Key.key(((Player) sender).getWorld().getName()).equals(world));
    }

    @Override
    public Audience server(String serverName) {
        return this.fallback.server(serverName);
    }

    @Override
    public ComponentFlattener flattener() {
        return this.fallback.flattener();
    }

    @Override
    public void close() {
        this.closed = true;
        this.fallback.close();
    }
}
