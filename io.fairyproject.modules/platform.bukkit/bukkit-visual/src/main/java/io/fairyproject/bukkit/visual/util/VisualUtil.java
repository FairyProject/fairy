package io.fairyproject.bukkit.visual.util;

import com.cryptomorin.xseries.XMaterial;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMultiBlockChange;
import com.google.common.collect.HashMultimap;
import io.fairyproject.Fairy;
import io.fairyproject.bukkit.metadata.Metadata;
import io.fairyproject.bukkit.reflection.resolver.FieldResolver;
import io.fairyproject.bukkit.reflection.resolver.MethodResolver;
import io.fairyproject.bukkit.reflection.resolver.ResolverQuery;
import io.fairyproject.bukkit.reflection.resolver.minecraft.NMSClassResolver;
import io.fairyproject.bukkit.reflection.resolver.minecraft.OBCClassResolver;
import io.fairyproject.bukkit.reflection.wrapper.MethodWrapper;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.mc.util.BlockPosition;
import io.fairyproject.metadata.MetadataKey;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

@UtilityClass
public class VisualUtil {

    public final MetadataKey<VisualContainer> FAKE_BLOCK_MAP = MetadataKey.create(Fairy.METADATA_PREFIX + "FakeBlockMap", VisualContainer.class);

    public void setFakeBlocks(Player player, Map<BlockPosition, XMaterial> blockMap, List<BlockPosition> replace, boolean send) {
        VisualContainer visualContainer = Metadata.provideForPlayer(player).getOrPut(FAKE_BLOCK_MAP, VisualContainer::new);
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
                mcPlayer.sendPacket(packet);
            }
        }
    }

    public void clearFakeBlocks(Player player, boolean send) {
        VisualContainer visualContainer = Metadata.provideForPlayer(player).getOrNull(FAKE_BLOCK_MAP);
        if (visualContainer == null) return;

        if (send) {
            setFakeBlocks(player, Collections.emptyMap(), new ArrayList<>(visualContainer.keySet()), true);
        } else {
            visualContainer.clear();
        }
    }

    private int getIdByMaterial(XMaterial material) {
        if (OldData.isCapable()) {
            return OldData.getId(material);
        }

        throw new UnsupportedOperationException();
    }

    @UtilityClass
    private static class NewData {

        private final Class<?> MAGIC_NUMBERS;
        private final MethodWrapper<?> FROM_LEGACY_DATA;
        private final MethodWrapper<?> GET_ID;

        static {
            Class<?> magicNumbers;
            MethodWrapper<?> fromLegacyData;
            MethodWrapper<?> getId;

            try {
                final NMSClassResolver CLASS_RESOLVER = new NMSClassResolver();
                final Class<?> blockStateType = CLASS_RESOLVER.resolve("world.level.block.state.BlockState", "IBlockData");
                final Class<?> blockType = CLASS_RESOLVER.resolve("world.level.block.Block", "Block");
                magicNumbers = new OBCClassResolver().resolve("util.CraftMagicNumbers");
                fromLegacyData = new MethodResolver(magicNumbers).resolve(blockStateType, 0, Material.class, byte.class);
                getId = new MethodResolver(blockType).resolve(blockStateType, 0, blockType);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
                magicNumbers = null;
                fromLegacyData = null;
                getId = null;
            }

            MAGIC_NUMBERS = magicNumbers;
            FROM_LEGACY_DATA = fromLegacyData;
            GET_ID = getId;
        }

//        public int getId(XMaterial material) {
//        }

        public static boolean isCapable() {
            return MAGIC_NUMBERS != null && FROM_LEGACY_DATA != null && GET_ID != null;
        }

    }

    @UtilityClass
    private static class OldData {

        private final MethodWrapper<?> BLOCK_GET_BY_ID_METHOD;
        private final MethodWrapper<?> FROM_LEGACY_DATA_METHOD;
        private final MethodWrapper<?> FROM_ID_METHOD;
        private final Object BLOCK_REGISTRY;

        static {
            MethodWrapper<?> blockGetById;
            MethodWrapper<?> fromLegacyData;
            MethodWrapper<?> fromId;
            Object blockRegistry;

            try {
                final NMSClassResolver CLASS_RESOLVER = new NMSClassResolver();
                final Class<?> blockType = CLASS_RESOLVER.resolve("Block");
                final Class<?> registryID = CLASS_RESOLVER.resolve("RegistryID");
                blockGetById = new MethodWrapper<>(blockType.getMethod("getById", int.class));
                fromLegacyData = new MethodWrapper<>(blockType.getMethod("fromLegacyData", int.class));
                blockRegistry = new FieldResolver(blockType).resolve(registryID, 0).get(0);
                fromId = new MethodResolver(registryID).resolveWrapper(new ResolverQuery(int.class, 0));
            } catch (Exception ex) {
                blockGetById = null;
                fromLegacyData = null;
                fromId = null;
                blockRegistry = null;
                ex.printStackTrace();
            }

            BLOCK_GET_BY_ID_METHOD = blockGetById;
            FROM_LEGACY_DATA_METHOD = fromLegacyData;
            FROM_ID_METHOD = fromId;
            BLOCK_REGISTRY = blockRegistry;
        }

        public boolean isCapable() {
            return BLOCK_GET_BY_ID_METHOD != null &&
                    FROM_LEGACY_DATA_METHOD != null &&
                    FROM_ID_METHOD != null &&
                    BLOCK_REGISTRY != null;
        }

        public int getId(XMaterial material) {
            Object block = BLOCK_GET_BY_ID_METHOD.invoke(null, material.getId());
            Object blockData = FROM_LEGACY_DATA_METHOD.invoke(block, material.getData());
            final Object invoke = FROM_ID_METHOD.invoke(BLOCK_REGISTRY, blockData);
            return (int) invoke;
        }

    }

}
