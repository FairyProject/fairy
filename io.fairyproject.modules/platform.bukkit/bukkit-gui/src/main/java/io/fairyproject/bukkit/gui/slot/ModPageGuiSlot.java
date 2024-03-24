package io.fairyproject.bukkit.gui.slot;

import com.cryptomorin.xseries.XSound;
import io.fairyproject.bukkit.gui.Gui;
import io.fairyproject.bukkit.gui.pane.PaginatedPane;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class ModPageGuiSlot implements GuiSlot {

    private final PaginatedPane pane;
    private final ItemStack itemStack;
    private final int mod;

    @Override
    public ItemStack getItemStack(@NotNull Player player, @NotNull Gui gui) {
        return this.itemStack;
    }

    @Override
    public void onInventoryClick(@NotNull InventoryClickEvent event, @NotNull Gui gui) {
        Player player = (Player) event.getWhoClicked();

        if (pane.getPage() + mod < 0) {
            sendCannotPrevious(player);
            return;
        } else if (pane.getPage() + mod >= pane.getMaxPage()) {
            sendCannotNext(player);
            return;
        }

        pane.setPage(pane.getPage() + mod);
        gui.update(player);

        XSound.UI_BUTTON_CLICK.play(player);
    }

    public void sendCannotPrevious(Player player) {
        player.sendMessage("§cYou cannot go back any further.");
        XSound.ENTITY_VILLAGER_NO.play(player);
    }

    public void sendCannotNext(Player player) {
        player.sendMessage("§cYou cannot go forward any further.");
        XSound.ENTITY_VILLAGER_NO.play(player);
    }
}
