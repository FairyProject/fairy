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

package io.fairyproject.bukkit.visual.sender;

import com.cryptomorin.xseries.XMaterial;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import com.google.common.collect.HashMultimap;
import io.fairyproject.Fairy;
import io.fairyproject.bukkit.metadata.Metadata;
import io.fairyproject.bukkit.nms.BukkitNMSManager;
import io.fairyproject.bukkit.visual.sender.impl.NewVisualData;
import io.fairyproject.bukkit.visual.sender.impl.OldVisualData;
import io.fairyproject.bukkit.visual.util.BlockPositionData;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.protocol.MCProtocol;
import io.fairyproject.mc.util.BlockPosition;
import io.fairyproject.metadata.MetadataKey;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class VisualBlockSender {

    public final MetadataKey<VisualContainer> fakeBlocksMetadataKey = MetadataKey.create(Fairy.METADATA_PREFIX + "FakeBlockMap", VisualContainer.class);
    private final List<VisualData> visualDataList;

    public VisualBlockSender(BukkitNMSManager nmsManager) {
        this.visualDataList = Arrays.asList(
                new OldVisualData(nmsManager),
                new NewVisualData(nmsManager)
        );
    }

    public void send(Player player, Map<BlockPosition, XMaterial> blockMap, List<BlockPosition> replace, boolean send) {
        VisualContainer visualContainer = Metadata.provideForPlayer(player).getOrPut(fakeBlocksMetadataKey, VisualContainer::new);
        HashMultimap<BlockPosition, BlockPositionData> map = HashMultimap.create();

        for (final Map.Entry<BlockPosition, XMaterial> entry : blockMap.entrySet()) {
            final BlockPosition blockPosition = entry.getKey();
            XMaterial materialData = entry.getValue();
            if (materialData == null) {
                materialData = XMaterial.AIR;
            }
            final XMaterial previous = visualContainer.put(blockPosition, materialData);
            if (send && previous != materialData) {
                final int x = blockPosition.getX();
                final int y = blockPosition.getY();
                final int z = blockPosition.getZ();
                final int chunkX = x >> 4;
                final int chunkY = y >> 4;
                final int chunkZ = z >> 4;
                final int posX = x - (chunkX << 4);
                final int posZ = z - (chunkZ << 4);
                map.put(new BlockPosition(chunkX, chunkY, chunkZ), new BlockPositionData(new BlockPosition(posX, y, posZ), materialData));
            }
        }
        for (final BlockPosition blockPosition : replace) {
            if (visualContainer.remove(blockPosition) != null) {
                final int x2 = blockPosition.getX();
                final int y2 = blockPosition.getY();
                final int z2 = blockPosition.getZ();
                final org.bukkit.block.Block blockData = player.getWorld().getBlockAt(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
                final Material type = blockData.getType();
                final byte data = blockData.getData();
                final int chunkX2 = x2 >> 4;
                final int chunkY2 = y2 >> 4;
                final int chunkZ2 = z2 >> 4;
                final int posX2 = x2 - (chunkX2 << 4);
                final int posZ2 = z2 - (chunkZ2 << 4);
                map.put(new BlockPosition(chunkX2, chunkY2, chunkZ2), new BlockPositionData(new BlockPosition(posX2, y2, posZ2), XMaterial.matchXMaterial(type.getId(), data).orElse(null)));
            }
        }

        MCPlayer mcPlayer = MCPlayer.from(player);
        if (send) {
            for (final Map.Entry<BlockPosition, Collection<BlockPositionData>> entry2 : map.asMap().entrySet()) {
                final BlockPosition chunkPosition = entry2.getKey();
                final Collection<BlockPositionData> blocks = entry2.getValue();

                WrapperPlayServerMultiBlockChange.EncodedBlock[] encodedBlocks = new WrapperPlayServerMultiBlockChange.EncodedBlock[blocks.size()];

                int i = 0;
                for (BlockPositionData positionData : blocks) {
                    BlockPosition b = positionData.getBlockPosition();
                    XMaterial materialData = positionData.getMaterial();

                    encodedBlocks[i] = new WrapperPlayServerMultiBlockChange.EncodedBlock(getIdByMaterial(materialData), b.getX(), b.getY(), b.getZ());
                    i++;
                }

                WrapperPlayServerMultiBlockChange packet = new WrapperPlayServerMultiBlockChange(new Vector3i(chunkPosition.getX(), chunkPosition.getY(), chunkPosition.getZ()), false, encodedBlocks);
                MCProtocol.sendPacket(mcPlayer, packet);
            }
        }
    }

    public void clearFakeBlocks(Player player, boolean send) {
        VisualContainer visualContainer = Metadata.provideForPlayer(player).getOrNull(fakeBlocksMetadataKey);
        if (visualContainer == null) return;

        if (send) {
            send(player, Collections.emptyMap(), new ArrayList<>(visualContainer.keySet()), true);
        } else {
            visualContainer.clear();
        }
    }

    private int getIdByMaterial(@NotNull XMaterial material) {
        for (VisualData visualData : this.visualDataList) {
            if (visualData.isCapable()) {
                return visualData.getId(material);
            }
        }

        throw new UnsupportedOperationException();
    }

}
