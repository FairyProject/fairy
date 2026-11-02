package io.fairyproject.bukkit.util.inventoryview;

import org.bukkit.inventory.Inventory;


public interface WrappedInventoryView {

    static WrappedInventoryView of(Object inventoryView) {
        if (ModernWrappedInventoryView.isModern()) {
            return new ModernWrappedInventoryView(inventoryView);
        } else {
            return new LegacyWrappedInventoryView(inventoryView);
        }
    }

    Inventory getTopInventory();

    Inventory getBottomInventory();

}
