package io.fairyproject.bukkit.mc;

import io.fairyproject.bukkit.version.BukkitVersionDecoder;
import io.fairyproject.mc.MCServer;
import io.fairyproject.mc.version.MCVersion;
import org.bukkit.Bukkit;
import org.bukkit.Server;

public class BukkitMCServer implements MCServer {

    private final Server server;
    private MCVersion mcVersion;

    public BukkitMCServer(Server server) {
        this.server = server;
    }

    @Override
    public boolean isMainThread() {
        return Bukkit.isPrimaryThread();
    }

    @Override
    public MCVersion getVersion() {
        if (mcVersion == null) {
            BukkitVersionDecoder bukkitVersionDecoder = BukkitVersionDecoder.create();
            this.mcVersion = bukkitVersionDecoder.decode(this.server);
        }
        return this.mcVersion;
    }
}
