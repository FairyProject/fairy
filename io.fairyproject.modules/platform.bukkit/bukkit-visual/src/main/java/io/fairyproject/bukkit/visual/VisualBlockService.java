/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
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

package io.fairyproject.bukkit.visual;

import com.cryptomorin.xseries.XMaterial;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.fairyproject.bukkit.Imanity;
import io.fairyproject.bukkit.listener.events.Events;
import io.fairyproject.bukkit.player.movement.MovementListener;
import io.fairyproject.bukkit.util.CoordXZ;
import io.fairyproject.bukkit.util.CoordinatePair;
import io.fairyproject.bukkit.visual.event.PreHandleVisualClaimEvent;
import io.fairyproject.bukkit.visual.event.PreHandleVisualEvent;
import io.fairyproject.bukkit.visual.type.VisualType;
import io.fairyproject.bukkit.visual.util.VisualUtil;
import io.fairyproject.container.PostInitialize;
import io.fairyproject.container.PreDestroy;
import io.fairyproject.container.Service;
import io.fairyproject.mc.util.BlockPosition;
import io.fairyproject.plugin.Plugin;
import io.fairyproject.plugin.PluginListenerAdapter;
import io.fairyproject.plugin.PluginManager;
import io.fairyproject.task.Task;
import io.fairyproject.task.TaskRunnable;
import io.fairyproject.util.terminable.Terminable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;

@Service
public class VisualBlockService implements TaskRunnable {

    private static final Logger LOGGER = LogManager.getLogger(VisualBlockService.class);

    private final Table<UUID, VisualPosition, VisualBlock> table = HashBasedTable.create();
    private LoadingCache<CoordinatePair, Optional<VisualBlockClaim>> claimCache;
    private Table<CoordinatePair, CoordXZ, VisualBlockClaim> claimPositionTable;
    private final Queue<VisualTask> visualTasks = new ConcurrentLinkedQueue<>();

    private VisualBlockGenerator mainGenerator;
    private final Map<Plugin, List<VisualBlockGenerator>> dynamicVisualGenerator = new ConcurrentHashMap<>();

    private boolean destroyed;

    @PostInitialize
    public void onPostInitialize() {
        this.claimPositionTable = HashBasedTable.create();
        this.claimCache = CacheBuilder.newBuilder()
                .maximumSize(8000)
                .build(new CacheLoader<CoordinatePair, Optional<VisualBlockClaim>>() {
                    @Override
                    public Optional<VisualBlockClaim> load(@NotNull CoordinatePair key) {
                        final int chunkX = key.getX() >> 4;
                        final int chunkZ = key.getZ() >> 4;
                        final int posX = key.getX() % 16;
                        final int posZ = key.getZ() % 16;
                        synchronized (claimPositionTable) {
                            return Optional.ofNullable(claimPositionTable.get(new CoordinatePair(key.getWorldName(), chunkX, chunkZ), new CoordXZ((byte) posX, (byte) posZ)));
                        }
                    }
                });
        Task.asyncRepeated(this, 1L);
        Imanity.registerMovementListener(new MovementListener() {
            @Override
            public void handleUpdateLocation(Player player, Location from, Location to) {
                handlePositionChanged(player, to);
            }
        })
        .ignoreSameBlock();

        this.mainGenerator = (player, location, positions) -> {
            final int minHeight = location.getBlockY() - 5;
            final int maxHeight = location.getBlockY() + 4;

            final int toX = location.getBlockX();
            final int toZ = location.getBlockZ();

            final Collection<VisualBlockClaim> claimCache = new HashSet<>();

            for (int x = toX - 7; x < toX + 7; x++) {
                for (int z = toZ - 7; z < toZ + 7; z++) {
                    final VisualBlockClaim color = getClaimAt(location.getWorld(), x, z);
                    PreHandleVisualClaimEvent claimEvent = new PreHandleVisualClaimEvent(player, color);

                    Events.call(claimEvent);

                    if (color != null && !claimEvent.isCancelled()) {
                        claimCache.add(color);
                    }
                }
            }

            if (!claimCache.isEmpty()) {
                final Iterator<VisualBlockClaim> claims = claimCache.iterator();
                while (claims.hasNext()) {

                    VisualBlockClaim claim = claims.next();
                    VisualType type = claim.getType();

                    for (final Vector edge : getEdges(claim)) {
                        if (Math.abs(edge.getBlockX() - toX) > 7) {
                            continue;
                        }
                        if (Math.abs(edge.getBlockZ() - toZ) > 7) {
                            continue;
                        }
                        final Location location2 = edge.toLocation(location.getWorld());
                        if (location2 == null) {
                            continue;
                        }
                        for (int y = minHeight; y <= maxHeight; y++) {
                            positions.add(new VisualPosition(location2.getBlockX(), y, location2.getBlockZ(), player.getWorld().getName(), type));
                        }
                    }

                    claims.remove();
                }
            }
        };

        PluginManager.INSTANCE.registerListener(new PluginListenerAdapter() {
            @Override
            public void onPluginDisable(Plugin plugin) {
                dynamicVisualGenerator.remove(plugin);
            }
        });
    }

    @PreDestroy
    public void onPreDestroy() {
        this.destroyed = true;
    }

    public void registerGenerator(VisualBlockGenerator blockGenerator) {
        Plugin plugin = PluginManager.INSTANCE.getPluginByClass(blockGenerator.getClass());

        if (plugin == null) {
            throw new IllegalArgumentException("Not a Plugin?");
        }
        List<VisualBlockGenerator> blockGenerators;
        if (this.dynamicVisualGenerator.containsKey(plugin)) {
            blockGenerators = this.dynamicVisualGenerator.get(plugin);
        } else {
            blockGenerators = new ArrayList<>();
            this.dynamicVisualGenerator.put(plugin, blockGenerators);
        }

        blockGenerators.add(blockGenerator);
        LOGGER.info(this.dynamicVisualGenerator.containsKey(plugin));
    }

    public void cacheClaim(VisualBlockClaim claim) {
        final World world = claim.getWorld();
        final int minX = Math.min(claim.getMaxX(), claim.getMinX());
        final int maxX = Math.max(claim.getMaxX(), claim.getMinX());
        final int minZ = Math.min(claim.getMaxZ(), claim.getMinZ());
        final int maxZ = Math.max(claim.getMaxZ(), claim.getMinZ());
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                final CoordinatePair worldPosition = new CoordinatePair(world, x, z);
                final CoordinatePair chunkPair = new CoordinatePair(world, x >> 4, z >> 4);
                final CoordXZ chunkPosition = new CoordXZ((byte) (x % 16), (byte) (z % 16));
                synchronized (claimPositionTable) {
                    claimPositionTable.put(chunkPair, chunkPosition, claim);
                }
                claimCache.invalidate(worldPosition);
            }
        }
    }

    public void clearAll(final Player player, final boolean send) {
        table.rowMap().remove(player.getUniqueId());
        VisualUtil.clearFakeBlocks(player, send);
    }

    public void clearVisualType(final Player player, final VisualType visualType, final boolean send) {
        clearVisualType(player, visualType, null, send);
    }

    public void clearVisualType(final Player player, final VisualType visualType, final Predicate<VisualBlock> predicate, final boolean send) {
        final List<BlockPosition> removeFromClient = new ArrayList<>();
        synchronized (table) {
            final Map<VisualPosition, VisualBlock> currentBlocks = table.row(player.getUniqueId());
            for (final Map.Entry<VisualPosition, VisualBlock> entry : new ArrayList<>(currentBlocks.entrySet())) {
                final VisualPosition blockPosition = entry.getKey();
                final VisualBlock visualBlock = entry.getValue();
                final VisualType blockVisualType = visualBlock.getVisualType();
                if (blockVisualType.equals(visualType) && (predicate == null || predicate.test(visualBlock))) {
                    removeFromClient.add(blockPosition);
                    currentBlocks.remove(blockPosition);
                }
            }
        }
        VisualUtil.setFakeBlocks(player, Collections.emptyMap(), removeFromClient, send);
    }

    public Map<BlockPosition, XMaterial> addVisualType(final Player player, final Collection<VisualPosition> locations, final boolean send) {
        final Map<BlockPosition, XMaterial> sendToClient = new HashMap<>();
        locations.removeIf(blockPosition -> {
            final World world = player.getWorld();
            final Block block = world.getBlockAt(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
            final Material material = block.getType();
            return material.isSolid();
        });
        synchronized (table) {
            for (VisualPosition blockPosition : locations) {
                VisualType visualType = blockPosition.getType();
                XMaterial material = visualType.generate(player, blockPosition);
                sendToClient.put(blockPosition, material);
                table.put(player.getUniqueId(), blockPosition, new VisualBlock(visualType, material, blockPosition));
            }
        }
        VisualUtil.setFakeBlocks(player, sendToClient, Collections.emptyList(), send);
        return sendToClient;
    }

    public Map<BlockPosition, XMaterial> setVisualType(final Player player, final Collection<VisualPosition> locations, final boolean send) {
        final Map<BlockPosition, XMaterial> sendToClient = new HashMap<>();
        final List<BlockPosition> removeFromClient = new ArrayList<>();
        locations.removeIf(blockPosition -> {
            final World world = player.getWorld();
            final Block block = world.getBlockAt(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
            final Material material = block.getType();
            return material.isSolid();
        });
        synchronized (table) {
            final Map<VisualPosition, VisualBlock> currentBlocks = table.row(player.getUniqueId());
            for (final Map.Entry<VisualPosition, VisualBlock> entry : new ArrayList<>(currentBlocks.entrySet())) {
                final VisualPosition blockPosition = entry.getKey();
                final VisualBlock visualBlock = entry.getValue();
                final VisualType blockVisualType = visualBlock.getVisualType();
                if (blockVisualType.equals(blockPosition.getType())) {
                    if (!locations.remove(blockPosition)) {
                        removeFromClient.add(blockPosition);
                        currentBlocks.remove(blockPosition);
                    }
                }
            }
            for (VisualPosition blockPosition : locations) {
                VisualType visualType = blockPosition.getType();
                XMaterial material = visualType.generate(player, blockPosition);
                sendToClient.put(blockPosition, material);
                table.put(player.getUniqueId(), blockPosition, new VisualBlock(visualType, material, blockPosition));
            }
        }
        VisualUtil.setFakeBlocks(player, sendToClient, removeFromClient, send);
        return sendToClient;
    }

    public VisualBlockClaim getClaimAt(final Location location) {
        return getClaimAt(location.getWorld(), location.getBlockX(), location.getBlockZ());
    }

    public VisualBlockClaim getClaimAt(final World world, final int x, final int z) {
        try {
            return claimCache.get(new CoordinatePair(world, x, z)).orElse(null);
        } catch (final Exception exception) {
            exception.printStackTrace();
            final int chunkX = x >> 4;
            final int chunkZ = z >> 4;
            final byte posX = (byte) (x % 16);
            final byte posZ = (byte) (z % 16);
            synchronized (claimPositionTable) {
                return claimPositionTable.get(new CoordinatePair(world, chunkX, chunkZ), new CoordXZ(posX, posZ));
            }
        }
    }

    public void handlePositionChanged(final Player player, final Location location) {
        if (this.claimPositionTable.isEmpty() && this.dynamicVisualGenerator.isEmpty()) {
            return;
        }

        PreHandleVisualEvent event = new PreHandleVisualEvent(player);
        Events.call(event);

        if (event.isCancelled()) {
            return;
        }

        final Set<VisualPosition> blockPositions = new HashSet<>();
        this.mainGenerator.generate(player, location, blockPositions);
        for (Map.Entry<Plugin, List<VisualBlockGenerator>> blockGenerators : this.dynamicVisualGenerator.entrySet()) {
            blockGenerators.getValue().forEach(blockGenerator -> blockGenerator.generate(player, location, blockPositions));
        }

        if (player.isOnline()) {
            visualTasks.removeIf(visualTask -> visualTask.getPlayer() == player);
            visualTasks.add(new VisualTask(player, blockPositions));
        }
    }

    public List<Vector> getEdges(VisualBlockClaim claim) {
        final int minX = Math.min(claim.getMinX(), claim.getMaxX());
        final int maxX = Math.max(claim.getMinX(), claim.getMaxX());
        final int minZ = Math.min(claim.getMinZ(), claim.getMaxZ());
        final int startX = minZ + 1;
        final int maxZ = Math.max(claim.getMinZ(), claim.getMaxZ());
        int capacity = (maxX - minX) * 4 + (maxZ - minZ) * 4;
        capacity += 4;
        if (capacity <= 0) {
            return new ArrayList<>();
        }
        final List<Vector> result = new ArrayList<>(capacity);
        final int minY = Math.min(claim.getMinY(), claim.getMaxY());
        final int maxY = Math.max(claim.getMinY(), claim.getMaxY());
        for (int z = minX; z <= maxX; ++z) {
            result.add(new Vector(z, minY, minZ));
            result.add(new Vector(z, minY, maxZ));
            result.add(new Vector(z, maxY, minZ));
            result.add(new Vector(z, maxY, maxZ));
        }
        for (int z = startX; z < maxZ; ++z) {
            result.add(new Vector(minX, minY, z));
            result.add(new Vector(minX, maxY, z));
            result.add(new Vector(maxX, minY, z));
            result.add(new Vector(maxX, maxY, z));
        }
        return result;
    }

    public void addVisualTask(Player player, VisualTask task) {
        this.visualTasks.removeIf(otherTask -> otherTask.getPlayer() == player);
        this.visualTasks.add(task);
    }

    public void clear() {
        Bukkit.getOnlinePlayers().forEach(player -> this.clearAll(player, true));
    }

    @Override
    public void run(Terminable terminable) {
        if (destroyed) {
            terminable.closeAndReportException();
            return;
        }

        VisualTask visualTask;
        while ((visualTask = visualTasks.poll()) != null) {
            this.setVisualType(visualTask.getPlayer(), visualTask.getBlockPositions(), true);
        }
    }
}
