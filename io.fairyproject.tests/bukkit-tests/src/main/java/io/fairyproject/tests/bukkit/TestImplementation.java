package io.fairyproject.tests.bukkit;

import io.fairyproject.bukkit.impl.annotation.ServerImpl;
import io.fairyproject.bukkit.impl.server.ServerImplementation;
import io.fairyproject.bukkit.listener.events.Events;
import io.fairyproject.bukkit.player.movement.MovementListener;
import io.fairyproject.bukkit.player.movement.impl.AbstractMovementImplementation;
import io.fairyproject.bukkit.player.movement.impl.BukkitMovementImplementation;
import io.fairyproject.mc.util.BlockPosition;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@ServerImpl(Integer.MAX_VALUE)
public class TestImplementation implements ServerImplementation {
    @Override
    public Entity getEntity(UUID uuid) {
        return BukkitTestingBase.SERVER.getEntity(uuid);
    }

    @Override
    public Entity getEntity(World world, int id) {
        return world.getEntities().stream()
                .filter(entity -> entity.getEntityId() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void showDyingNPC(Player player) {
        new RuntimeException("showDyingNPC(" + player.getName() + ")").printStackTrace();
    }

    @Override
    public Object toBlockNMS(MaterialData materialData) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public List<Player> getPlayerRadius(Location location, double radius) {
        return Objects.requireNonNull(location.getWorld()).getNearbyEntities(location, radius / 2, radius / 2, radius / 2)
                .stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .collect(Collectors.toList());
    }

    @Override
    public void setFakeBlocks(Player player, Map<BlockPosition, MaterialData> positions, List<BlockPosition> toRemove, boolean send) {
        new RuntimeException("setFakeBlocks(" + player.getName() + "," + positions + "," + toRemove + "," + send + ")").printStackTrace();
    }

    @Override
    public void clearFakeBlocks(Player player, boolean send) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void sendActionBar(Player player, String message) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public float getBlockSlipperiness(Material material) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void sendEntityTeleport(Player player, Location location, int id) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void sendEntityAttach(Player player, int type, int toAttach, int attachTo) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void setSkullGameProfile(ItemMeta itemMeta, Player player) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean isServerThread() {
        return BukkitTestingBase.SERVER.isPrimaryThread();
    }

    @Override
    public boolean callMoveEvent(Player player, Location from, Location to) {
        PlayerMoveEvent event = new PlayerMoveEvent(player, from, to);
        Events.call(event);
        return !event.isCancelled();
    }

    @Override
    public AbstractMovementImplementation movement(MovementListener movementListener) {
        return new BukkitMovementImplementation(movementListener);
    }
}
