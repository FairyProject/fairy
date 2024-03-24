package io.fairyproject.bukkit.gui.template;

import com.cryptomorin.xseries.XMaterial;
import io.fairyproject.bukkit.gui.Gui;
import io.fairyproject.bukkit.gui.GuiFactory;
import io.fairyproject.bukkit.gui.pane.NormalPane;
import io.fairyproject.bukkit.gui.pane.Pane;
import io.fairyproject.bukkit.gui.slot.GuiSlot;
import io.fairyproject.bukkit.util.items.ItemBuilder;
import io.fairyproject.container.InjectableComponent;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@InjectableComponent
@RequiredArgsConstructor
public class SetItemGui {

    private final GuiFactory guiFactory;

    public Gui create(@Nullable ItemStack current, Consumer<ItemStack> callback) {
        Gui gui = guiFactory.create(Component.text("Set Item", NamedTextColor.YELLOW));

        NormalPane pane = Pane.normal(3);
        pane.setSlot(4, 1, GuiSlot.itemSelector(pane, 4, 1)
                .currentItem(current)
                .emptyItem(ItemBuilder.of(XMaterial.BARRIER)
                        .name("&cEmpty")
                        .build())
                .updateCallback(callback)
                .build());

        pane.fillEmptySlots(GuiSlot.of(XMaterial.BLACK_STAINED_GLASS_PANE, " "));
        gui.addPane(pane);

        return gui;
    }

}
