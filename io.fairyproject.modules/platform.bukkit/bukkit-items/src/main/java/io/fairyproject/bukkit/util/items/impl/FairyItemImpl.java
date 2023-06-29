package io.fairyproject.bukkit.util.items.impl;

import io.fairyproject.bukkit.util.items.FairyItem;
import io.fairyproject.bukkit.util.items.FairyItemRef;
import io.fairyproject.bukkit.util.items.FairyItemRegistry;
import io.fairyproject.bukkit.util.items.ItemBuilder;
import io.fairyproject.bukkit.util.items.behaviour.ItemBehaviour;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.metadata.MetadataMap;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Getter
public class FairyItemImpl implements FairyItem {

    private final FairyItemRegistry itemRegistry;
    private final String name;
    private final MetadataMap metadataMap;
    private final List<ItemBehaviour> behaviours;
    private final Function<MCPlayer, ItemBuilder> itemProvider;

    private boolean closed;

    public FairyItemImpl(
            @NonNull FairyItemRegistry itemRegistry,
            @NonNull String name,
            @NonNull MetadataMap metadataMap,
            @NonNull List<ItemBehaviour> behaviours,
            @NonNull Function<MCPlayer, ItemBuilder> itemProvider) {
        this.itemRegistry = itemRegistry;
        this.name = name;
        this.metadataMap = metadataMap;
        this.behaviours = behaviours;
        this.itemProvider = itemProvider;
        this.closed = true;
    }

    @Override
    public @NotNull Iterable<ItemBehaviour> getBehaviours() {
        return Collections.unmodifiableList(this.behaviours);
    }

    @Override
    public @NotNull ItemBuilder provide(@NotNull MCPlayer mcPlayer) {
        return this.itemProvider.apply(mcPlayer)
                .clone()
                .transformItemStack(itemStack -> this.itemRegistry.set(itemStack, this));
    }

    @Override
    public void init() {
        this.behaviours.forEach(behaviour -> behaviour.init(this));
        this.closed = false;
    }

    @Override
    public void close() {
        this.behaviours.forEach(ItemBehaviour::unregister);
        this.closed = true;
    }

    @Override
    public boolean isSimilar(@NotNull ItemStack itemStack) {
        return this.itemRegistry.get(itemStack) == this;
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }
}
