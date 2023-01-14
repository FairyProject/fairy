package io.fairyproject.bukkit.mc;

import io.fairyproject.bukkit.nms.BukkitNMSManager;
import io.fairyproject.bukkit.version.BukkitVersionDecoder;
import io.fairyproject.mc.MCEntity;
import io.fairyproject.mc.MCServer;
import io.fairyproject.mc.version.MCVersion;
import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.util.UUID;

public class BukkitMCServer implements MCServer {

    private final Server server;

    public BukkitMCServer(Server server) {
        this.server = server;
    }

    @Override
    public boolean isMainThread() {
        return Bukkit.isPrimaryThread();
    }

    @Override
    public MCVersion getVersion() {
        BukkitVersionDecoder bukkitVersionDecoder = BukkitVersionDecoder.create();

        return bukkitVersionDecoder.decode(this.server);
    }
}
