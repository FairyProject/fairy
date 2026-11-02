package io.fairyproject.bukkit.util.inventoryview;

import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

@RequiredArgsConstructor
public class ModernWrappedInventoryView implements WrappedInventoryView {

    private static Boolean MODERN;

    public static synchronized boolean isModern() {
        if (MODERN == null) {
            try {
                Class<?> inventoryViewClass = Class.forName("org.bukkit.inventory.InventoryView");
                MODERN = inventoryViewClass.isInterface();
            } catch (ClassNotFoundException e) {
                MODERN = false;
            }
        }
        return MODERN;
    }

    private final Object inventoryView;

    @Override
    public Inventory getTopInventory() {
        return ((InventoryView) inventoryView).getTopInventory();
    }

    @Override
    public Inventory getBottomInventory() {
        return ((InventoryView) inventoryView).getBottomInventory();
    }

}
