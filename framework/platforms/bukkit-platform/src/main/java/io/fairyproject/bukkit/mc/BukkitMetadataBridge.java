package io.fairyproject.bukkit.mc;

import io.fairyproject.container.InjectableComponent;
import io.fairyproject.data.MetaStorage;
import io.fairyproject.mc.data.MCMetadata;
import io.fairyproject.mc.data.MCMetadataBridge;
import io.fairyproject.mc.util.BlockPosition;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;

@InjectableComponent
public class BukkitMetadataBridge implements MCMetadataBridge {

    public BukkitMetadataBridge() {
        MCMetadata.BRIDGE = this;
    }

    @Override
    public @NotNull MetaStorage provide(@NotNull Object object) {
        if (object instanceof Player) {
            Player player = (Player) object;

            return MCMetadata.providePlayer(player.getUniqueId());
        }

        if (object instanceof World) {
            World world = (World) object;

            return MCMetadata.provideWorld(world.getName());
        }

        if (object instanceof Entity) {
            Entity entity = (Entity) object;

            return MCMetadata.provideEntity(entity.getUniqueId());
        }

        if (object instanceof Block) {
            Block block = (Block) object;
            BlockVector blockVector = block.getLocation().toVector().toBlockVector();

            return MCMetadata.provideBlock(new BlockPosition(blockVector.getBlockX(), blockVector.getBlockY(), blockVector.getBlockZ()));
        }

        throw new IllegalArgumentException("Unsupported object type: " + object.getClass().getName());
    }

}
