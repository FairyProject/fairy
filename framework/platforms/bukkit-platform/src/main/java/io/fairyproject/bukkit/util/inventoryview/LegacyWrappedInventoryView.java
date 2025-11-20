package io.fairyproject.bukkit.util.inventoryview;

import io.fairyproject.bukkit.reflection.resolver.MethodResolver;
import io.fairyproject.bukkit.reflection.wrapper.MethodWrapper;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.Inventory;

@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class LegacyWrappedInventoryView implements WrappedInventoryView {

    private static final MethodWrapper<Inventory> GET_TOP_INVENTORY;
    private static final MethodWrapper<Inventory> GET_BOTTOM_INVENTORY;

    static {
        MethodWrapper<Inventory> getTopInventory;
        MethodWrapper<Inventory> getBottomInventory;
        try {
            Class<?> inventoryViewClass = Class.forName("org.bukkit.inventory.InventoryView");
            MethodResolver methodResolver = new MethodResolver(inventoryViewClass);
            getTopInventory = methodResolver.resolveWrapper("getTopInventory");
            getBottomInventory = methodResolver.resolveWrapper("getBottomInventory");
        } catch (Exception ex) {
            getTopInventory = null;
            getBottomInventory = null;
        }

        GET_TOP_INVENTORY = getTopInventory;
        GET_BOTTOM_INVENTORY = getBottomInventory;
    }

    private final Object inventoryView;

    @Override
    public Inventory getTopInventory() {
        return GET_TOP_INVENTORY.invoke(inventoryView);
    }

    @Override
    public Inventory getBottomInventory() {
        return GET_BOTTOM_INVENTORY.invoke(inventoryView);
    }
}
