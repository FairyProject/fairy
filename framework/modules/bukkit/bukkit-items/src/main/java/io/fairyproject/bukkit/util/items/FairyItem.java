package io.fairyproject.bukkit.util.items;

import io.fairyproject.bukkit.util.items.behaviour.ItemBehaviour;
import io.fairyproject.bukkit.util.items.impl.FairyItemImpl;
import io.fairyproject.container.Autowired;
import io.fairyproject.data.MetaKey;
import io.fairyproject.data.MetaStorage;
import io.fairyproject.mc.MCPlayer;
import io.fairyproject.util.terminable.Terminable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public interface FairyItem extends Terminable {

    static Builder builder(@NotNull String name) {
        return new Builder(name);
    }

    @NotNull String getName();

    @NotNull MetaStorage getMetaStorage();

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
        private Function<MCPlayer, ItemBuilder> itemProvider;
        private final MetaStorage metaStorage = MetaStorage.create();
        private final List<ItemBehaviour> behaviours = new ArrayList<>();

        private Builder(@NotNull String name) {
            this.name = name;
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

        public Builder transformMeta(@NotNull Consumer<MetaStorage> consumer) {
            consumer.accept(this.metaStorage);
            return this;
        }

        public <T> Builder put(@NotNull MetaKey<T> metadataKey, @NotNull T value) {
            this.metaStorage.put(metadataKey, value);
            return this;
        }

        @Deprecated
        public FairyItem build() {
            FairyItem fairyItem = new FairyItemImpl(REGISTRY, this.name, this.metaStorage, this.behaviours, this.itemProvider);
            REGISTRY.register(fairyItem);

            return fairyItem;
        }

        @Deprecated
        public FairyItem create() {
            return new FairyItemImpl(REGISTRY, this.name, this.metaStorage, this.behaviours, this.itemProvider);
        }

        public FairyItem create(FairyItemRegistry itemRegistry) {
            FairyItem fairyItem = new FairyItemImpl(itemRegistry, this.name, this.metaStorage, this.behaviours, this.itemProvider);
            itemRegistry.register(fairyItem);

            return fairyItem;
        }
    }

}
