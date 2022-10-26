package io.fairyproject.bukkit.util.items;

import io.fairyproject.bukkit.util.BukkitUtil;
import io.fairyproject.bukkit.util.items.behaviour.ItemBehaviour;
import io.fairyproject.bukkit.util.items.impl.FairyItemImpl;
import io.fairyproject.container.Autowired;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.metadata.MetadataMap;
import io.fairyproject.util.terminable.Terminable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface FairyItem extends Terminable {

    static Builder builder(@NotNull String name) {
        Plugin plugin = BukkitUtil.getCurrentPlugin(4);

        return new Builder(name)
                .plugin(plugin);
    }

    @NotNull String getName();

    @NotNull Plugin getPlugin();

    @NotNull MetadataMap getMetadataMap();

    @NotNull Iterable<ItemBehaviour> getBehaviours();

    @NotNull ItemBuilder provide(@NotNull MCPlayer mcPlayer);

    default @NotNull ItemBuilder provide(@NotNull Player player) {
        return this.provide(MCPlayer.from(player));
    }

    default @NotNull ItemStack provideItemStack(@NotNull MCPlayer mcPlayer) {
        return this.provide(mcPlayer).build();
    }

    default @NotNull ItemStack provideItemStack(@NotNull Player player) {
        return this.provideItemStack(MCPlayer.from(player));
    }

    boolean isSimilar(@NotNull ItemStack itemStack);

    @ApiStatus.Internal
    void init();

    @ApiStatus.Internal
    @Override
    void close() throws Exception;

    @ApiStatus.Internal
    @Nullable
    @Override
    default Exception closeSilently() {
        return Terminable.super.closeSilently();
    }

    @ApiStatus.Internal
    @Override
    default void closeAndReportException() {
        Terminable.super.closeAndReportException();
    }

    class Builder {

        @Autowired
        private static FairyItemRegistry REGISTRY;

        private final String name;
        private Plugin plugin;
        private Function<MCPlayer, ItemBuilder> itemProvider;
        private final MetadataMap metadataMap = MetadataMap.create();
        private final List<ItemBehaviour> behaviours = new ArrayList<>();

        private Builder(@NotNull String name) {
            this.name = name;
        }

        public Builder plugin(@NotNull Plugin plugin) {
            this.plugin = plugin;
            return this;
        }

        public Builder behaviour(@NotNull ItemBehaviour behaviour) {
            this.behaviours.add(behaviour);
            return this;
        }

        public Builder item(@NotNull ItemStack itemStack) {
            return this.item(mcPlayer -> ItemBuilder.of(itemStack));
        }

        public Builder item(@NotNull ItemBuilder itemBuilder) {
            return this.item(mcPlayer -> itemBuilder);
        }

        public Builder item(@NotNull Function<MCPlayer, ItemBuilder> itemProvider) {
            this.itemProvider = itemProvider;
            return this;
        }

        public FairyItem build() {
            FairyItem fairyItem = new FairyItemImpl(this.name, this.plugin, this.metadataMap, this.behaviours, this.itemProvider);
            REGISTRY.register(fairyItem);

            return fairyItem;
        }
    }

}
