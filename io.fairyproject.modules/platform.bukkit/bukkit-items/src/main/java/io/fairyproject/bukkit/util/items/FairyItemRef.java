package io.fairyproject.bukkit.util.items;

import io.fairyproject.container.Autowired;
import lombok.experimental.UtilityClass;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Deprecated
@UtilityClass
public class FairyItemRef {

    @Autowired
    private FairyItemRegistry REGISTRY;


    public ItemStack set(@NotNull ItemStack itemStack, @NotNull FairyItem fairyItem) {
        return REGISTRY.set(itemStack, fairyItem);
    }

    public FairyItem get(@Nullable ItemStack itemStack) {
        return REGISTRY.get(itemStack);
    }

}
