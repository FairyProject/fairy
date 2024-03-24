package io.fairyproject.bukkit.gui.slot;

import io.fairyproject.bukkit.gui.Gui;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class StaticGuiSlot implements GuiSlot {

    private final ItemStack itemStack;
    @Nullable
    private final Consumer<InventoryClickEvent> clickCallback;

    @Override
    public ItemStack getItemStack(@NotNull Player player, @NotNull Gui gui) {
        return itemStack;
    }

    @Override
    public void onInventoryClick(@NotNull InventoryClickEvent event, @NotNull Gui gui) {
        if (clickCallback != null)
            clickCallback.accept(event);
    }
}
