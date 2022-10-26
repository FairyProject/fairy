package io.fairyproject.bukkit.util.items;

import io.fairyproject.bukkit.nbt.NBTKey;
import io.fairyproject.bukkit.nbt.NBTModifier;
import io.fairyproject.container.Autowired;
import lombok.experimental.UtilityClass;
import org.bukkit.inventory.ItemStack;

@UtilityClass
public class FairyItemRef {

    @Autowired
    private FairyItemRegistry REGISTRY;

    public final NBTKey FAIRY_ITEM = NBTKey.create("fairy", "item", "name");

    public ItemStack set(ItemStack itemStack, FairyItem fairyItem) {
        return NBTModifier.get().setTag(itemStack, FAIRY_ITEM, fairyItem.getName());
    }

    public FairyItem get(ItemStack itemStack) {
        String key = NBTModifier.get().getString(itemStack, FAIRY_ITEM);
        return key == null ? null : REGISTRY.get(key);
    }

}
