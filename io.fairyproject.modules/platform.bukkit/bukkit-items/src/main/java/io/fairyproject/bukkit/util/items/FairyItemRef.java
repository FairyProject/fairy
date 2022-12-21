package io.fairyproject.bukkit.util.items;

import io.fairyproject.bukkit.nbt.NBTKey;
import io.fairyproject.bukkit.nbt.NBTModifier;
import io.fairyproject.container.Autowired;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class FairyItemRef {

    @Autowired
    private FairyItemRegistry REGISTRY;

    public final NBTKey FAIRY_ITEM = NBTKey.create("fairy", "item", "name");

    public ItemStack set(@NotNull ItemStack itemStack, @NotNull FairyItem fairyItem) {
        return NBTModifier.get().setTag(itemStack, FAIRY_ITEM, fairyItem.getName());
    }

    public FairyItem get(@Nullable ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return null;
        String key = NBTModifier.get().getString(itemStack, FAIRY_ITEM);
        return key == null ? null : REGISTRY.get(key);
    }

}
